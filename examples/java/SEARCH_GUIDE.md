# Project search guide (SSPL)

This guide explains how a SmartSearch AI project search works and points to the example that
shows each part. The examples are in [`src/main/java/examples/search`](src/main/java/examples/search).

## The request

A search is one request to `POST {SMARTSEARCH_API_BASE_URL}/core/projects/{projectId}/search`
with a JSON body in **SSPL**, the SmartSearch search request language. You never write the JSON:
`SearchQuery.builder()` writes it, checks every value, and throws `IllegalArgumentException`
before sending when something is invalid. `SearchQuery.toJson()` shows the exact body.

```java
SearchQuery query = SearchQuery.builder()
        .q("star wars")                                   // what the user typed
        .responseFields("title", "release_date")          // what each hit returns
        .filter(Filter.term("original_language", "en"))   // a yes/no condition
        .size(10)                                         // how many hits
        .build();
SearchResult result = ss.search().search(projectId, query);
```

An SSPL request has five parts:

| Part | Builder methods | Example |
|---|---|---|
| **Query** | `q`, `fields` | `FirstSearch`, `ChooseSearchedAndReturnedFields` |
| **What to return** | `responseFields`, `from`, `size`, `sort`, `highlight` | `PageThroughResults`, `SortResults`, `HighlightMatches` |
| **Filters** | `filter`, `anyOf`, `exclude` with `Filter.*` | `FilterBy...`, `ExcludeResults`, `CombineFiltersWithAnyOf` |
| **Ranking** | `termBoost`, `neuralMode`, `normalization`, `rankWindow`, `rankConstant`, `neuralMatches`, `rerank*`, `precision` | `BoostTermValues`, `KeywordVsSemanticVsHybrid`, `RerankResults`, `PrecisionLevels` |
| **Query handling and extras** | `autoCorrect`, `wildcard`, `trimQuery`, `removeSpecialChars`, `disableQueryExpansion`, `termsAgg`, `cardinalityAgg` | `CorrectSpelling`, `WildcardAndPrefixSearch`, `CleanUpUserInput`, `TurnOffQueryExpansion`, `FacetCounts` |

## The response

`SearchResult` gives you the envelope and the result:

| Method | What it is |
|---|---|
| `code()` | 1 on success |
| `message()` | Short status text ("Success") |
| `statusCode()` | Status the server reported in the body (0 when absent) |
| `searchId()` | Identifies this search; quote it when reporting a problem |
| `rankingId()` | Identifies the ranking used, when the server reports one (may be null) |
| `effectiveNeuralMode()` | The search technique that actually ran |
| `warning()` | Set when the server changed something you asked for |
| `errorMessage()` | Extra detail about a failure, when present |
| `result()` | `hits.hits` (the documents) and `aggregations` (facets) |

Each hit has `_source` (the fields you asked for), `_score` (when the order is by score) and
`highlight` (when you asked for it). Count the hits you received; the response does not carry a
reliable total number of matches.

When the server refuses a request, the SDK throws `SearchException` with the HTTP status
(`statusCode()`, 0 when no response arrived) and the server's `serverMessage()` and
`errorMessage()`. See `HandleErrorsAndWarnings`.

## Filters

Filters are yes/no conditions. They remove documents and do not change the scores of the rest.

| Filter | Keeps documents where | Example |
|---|---|---|
| `Filter.term(field, value)` | the field equals the value exactly | `FilterByExactValue` |
| `Filter.terms(field, v1, v2, ...)` | the field equals any of the values | `FilterByAnyOfSeveralValues` |
| `Filter.range(field, gte, lte, gt, lt)` | a number (or date) is in range; `null` = no bound | `FilterByNumericRange`, `FilterByDateRange` |
| `Filter.exists(field)` | the field has a value | `FilterWhereFieldExists` |
| `Filter.match(field, text)` | the text field contains the words | `FilterByFullTextMatch` |
| `Filter.matchPhrase(field, phrase)` | the text field contains the words together, in order | `FilterByExactPhrase` |

Combine them: every `filter(f)` must match (AND), at least one inside `anyOf(f1, f2, ...)` must
match (OR), and no `exclude(f)` may match (NOT). See `CombineFiltersWithAnyOf`.

For a list of objects, such as `genres: [{"id": 35, "name": "Comedy"}]`, filter on the property:
`genres.name`.

## Search techniques

`neuralMode(...)` chooses how documents are found and scored. Without it the project's default is
used.

| Mode | What it does | Good for |
|---|---|---|
| `BM25` | **Keyword search**: scores documents by the query words they contain | Exact terms, codes, names; sorting, paging, boosts |
| `A_KNN` | **Semantic search**: finds documents close in meaning, even with no shared words | Natural-language questions, descriptions |
| `A_KNN_AND_BM25` | **Hybrid**: runs both and fuses the ranked lists | General search boxes |

Semantic and hybrid search need a project with embeddings. The enum also lists
`EXACT_AND_BM25_FUSED` and `EXACT_AND_BM25_RANKING`, which only some projects support. When the
project cannot run the mode you asked for, the server runs another one, reports it in
`effectiveNeuralMode()` and explains in `warning()`. See `KeywordVsSemanticVsHybrid`,
`TuneHybridSearch` (fusion settings) and `LimitSemanticMatches` (how many semantic candidates).

## Reranking

**Reranking** is a second pass in which an AI model reads the query with each of the top
candidates and reorders them by relevance. `rerank(true)` / `rerank(false)` / `rerank(null)`
(project default) choose it; `rerankFirstStageSize` and `rerankMinScore` tune it. See
`RerankResults` and `TuneReranking`.

The reranker orders by relevance, so turn it off (`rerank(false)`) when something else must
decide the order:

- **sorting** (`SortResults`): reranking overrides the sort;
- **paging** (`PageThroughResults`): reranking does not apply the `from` offset;
- **business boosts** (`BoostTermValues`): reranking can undo a boost.

## Precision

`precision(1..11)` sets how strictly the query words must match: 1 is broad (more results), 11 is
strict (fewer, more exact results). It needs a project that uses a precision search template.
See `PrecisionLevels`.

## Facets

`termsAgg` (the most common values of a field, with counts) and `cardinalityAgg` (how many
distinct values) power facet lists. They need a project **without document security**: on a
project where each document carries access rules, the server refuses aggregations so that counts
cannot reveal documents the caller may not see. See `FacetCounts`.

## Several searches, and use cases

- `multiSearch(projectId, queries)` sends several searches in one request
  (`POST .../core/projects/{projectId}/mSearch`) and returns one result per query in order. It
  does not apply each query's `size`, so take the first n hits yourself. See
  `RunSeveralSearchesAtOnce`.
- `searchByUsecase(usecaseId, query)` (`POST .../core/usecases/{usecaseId}/search`) searches with
  the saved settings of a **use case** of a project. See `SearchThroughUseCase`.

## Workplace: search, query or chat

Project search (this guide) searches a project with SSPL. **Workplace** is the separate search and
answer engine for company knowledge in workspaces, with three calls:

- **search**: the matching documents, no language model; for a results list (`SearchWorkplaceAsService`).
- **query**: one question, one answer written from the documents with its sources, or the sources
  only; for a question box or your own model (`AskAQuestion`, `RetrieveSourcesOnly`, `StreamAnAnswer`).
- **chat**: like query, in a conversation where follow-ups build on earlier turns
  (`ChatWithFollowUpQuestions`, `StreamChatAnswer`).

## Example map

| Capability | Example |
|---|---|
| First search, reading results | `FirstSearch` |
| Searched versus returned fields | `ChooseSearchedAndReturnedFields` |
| Paging | `PageThroughResults` |
| Sorting | `SortResults` |
| Exact value | `FilterByExactValue` |
| Several values | `FilterByAnyOfSeveralValues` |
| Numeric range | `FilterByNumericRange` |
| Date range | `FilterByDateRange` |
| Field exists | `FilterWhereFieldExists` |
| Full-text match | `FilterByFullTextMatch` |
| Exact phrase | `FilterByExactPhrase` |
| Excluding | `ExcludeResults` |
| AND, OR, NOT | `CombineFiltersWithAnyOf` |
| Term boosting | `BoostTermValues` |
| Highlighting | `HighlightMatches` |
| Wildcard and prefix | `WildcardAndPrefixSearch` |
| Spelling correction | `CorrectSpelling` |
| Cleaning user input | `CleanUpUserInput` |
| Query expansion off | `TurnOffQueryExpansion` |
| Facet and distinct counts | `FacetCounts` |
| Keyword, semantic, hybrid | `KeywordVsSemanticVsHybrid` |
| Hybrid fusion | `TuneHybridSearch` |
| Semantic matches | `LimitSemanticMatches` |
| Reranking on, off, default | `RerankResults` |
| Rerank candidates and minimum score | `TuneReranking` |
| Precision levels | `PrecisionLevels` |
| Multi-search | `RunSeveralSearchesAtOnce` |
| Use-case search | `SearchThroughUseCase` |
| Errors and warnings | `HandleErrorsAndWarnings` |

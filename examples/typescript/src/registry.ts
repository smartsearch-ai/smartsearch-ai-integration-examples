export const examples = [
  {
    step: 1,
    name: "connect_and_check_access",
    description: "What a service key is; connect and check what the key may do",
    load: () => import("./gettingstarted/connect_and_check_access.js"),
  },
  {
    step: 2,
    name: "first_search",
    description:
      "Build a request, send it, read the hits and the response envelope",
    load: () => import("./search/first_search.js"),
  },
  {
    step: 3,
    name: "choose_searched_and_returned_fields",
    description: "Where the query is looked for versus what each hit returns",
    load: () => import("./search/choose_searched_and_returned_fields.js"),
  },
  {
    step: 4,
    name: "page_through_results",
    description: "Paging with `from_` and `size` (reranking off)",
    load: () => import("./search/page_through_results.js"),
  },
  {
    step: 5,
    name: "sort_results",
    description: "Sort by a field instead of relevance (reranking off)",
    load: () => import("./search/sort_results.js"),
  },
  {
    step: 6,
    name: "filter_by_exact_value",
    description:
      "Yes/no conditions on an exact value, including properties inside lists",
    load: () => import("./search/filter_by_exact_value.js"),
  },
  {
    step: 7,
    name: "filter_by_any_of_several_values",
    description: "One field, any of several values (multi-select facets)",
    load: () => import("./search/filter_by_any_of_several_values.js"),
  },
  {
    step: 8,
    name: "filter_by_numeric_range",
    description: "At least, at most, between",
    load: () => import("./search/filter_by_numeric_range.js"),
  },
  {
    step: 9,
    name: "filter_by_date_range",
    description: "Date ranges",
    load: () => import("./search/filter_by_date_range.js"),
  },
  {
    step: 10,
    name: "filter_where_field_exists",
    description: "Only documents that have a value in a field",
    load: () => import("./search/filter_where_field_exists.js"),
  },
  {
    step: 11,
    name: "filter_by_full_text_match",
    description: "Words anywhere in a text field",
    load: () => import("./search/filter_by_full_text_match.js"),
  },
  {
    step: 12,
    name: "filter_by_exact_phrase",
    description: "Words together and in order",
    load: () => import("./search/filter_by_exact_phrase.js"),
  },
  {
    step: 13,
    name: "exclude_results",
    description: "Remove documents that match a condition",
    load: () => import("./search/exclude_results.js"),
  },
  {
    step: 14,
    name: "combine_filters_with_any_of",
    description: "AND, OR and NOT together",
    load: () => import("./search/combine_filters_with_any_of.js"),
  },
  {
    step: 15,
    name: "boost_term_values",
    description: "Move preferred documents up without removing others",
    load: () => import("./search/boost_term_values.js"),
  },
  {
    step: 16,
    name: "highlight_matches",
    description: "Show why a result matched",
    load: () => import("./search/highlight_matches.js"),
  },
  {
    step: 17,
    name: "wildcard_and_prefix_search",
    description: "`avat*` style queries",
    load: () => import("./search/wildcard_and_prefix_search.js"),
  },
  {
    step: 18,
    name: "correct_spelling",
    description: "Find results despite typos",
    load: () => import("./search/correct_spelling.js"),
  },
  {
    step: 19,
    name: "clean_up_user_input",
    description: "Trim spaces and remove special characters from typed text",
    load: () => import("./search/clean_up_user_input.js"),
  },
  {
    step: 20,
    name: "turn_off_query_expansion",
    description: "Stop related-word expansion for one request",
    load: () => import("./search/turn_off_query_expansion.js"),
  },
  {
    step: 21,
    name: "facet_counts",
    description: "Value counts, distinct counts, drill-down",
    load: () => import("./search/facet_counts.js"),
  },
  {
    step: 22,
    name: "keyword_vs_semantic_vs_hybrid",
    description: "The three search techniques, and checking which one ran",
    load: () => import("./search/keyword_vs_semantic_vs_hybrid.js"),
  },
  {
    step: 23,
    name: "tune_hybrid_search",
    description: "How keyword and semantic results are fused",
    load: () => import("./search/tune_hybrid_search.js"),
  },
  {
    step: 24,
    name: "limit_semantic_matches",
    description: "How many close-in-meaning documents to consider and keep",
    load: () => import("./search/limit_semantic_matches.js"),
  },
  {
    step: 25,
    name: "rerank_results",
    description: "AI reranking on, off, and the project default",
    load: () => import("./search/rerank_results.js"),
  },
  {
    step: 26,
    name: "tune_reranking",
    description: "How many candidates to rerank; a minimum score",
    load: () => import("./search/tune_reranking.js"),
  },
  {
    step: 27,
    name: "precision_levels",
    description: "Broad versus strict matching",
    load: () => import("./search/precision_levels.js"),
  },
  {
    step: 28,
    name: "run_several_searches_at_once",
    description: "Several searches in one round trip",
    load: () => import("./search/run_several_searches_at_once.js"),
  },
  {
    step: 29,
    name: "search_through_use_case",
    description: "Search with a saved configuration",
    load: () => import("./search/search_through_use_case.js"),
  },
  {
    step: 30,
    name: "handle_errors_and_warnings",
    description: "Invalid requests, refusals, and adjusted answers",
    load: () => import("./search/handle_errors_and_warnings.js"),
  },
  {
    step: 31,
    name: "search_workplace_as_service",
    description:
      "Workplace concepts; search, query and chat; the documents that match",
    load: () => import("./workplace/search_workplace_as_service.js"),
  },
  {
    step: 32,
    name: "filter_workplace_by_source_and_title",
    description: "Limit a search to one source; filter on a document field",
    load: () => import("./workplace/filter_workplace_by_source_and_title.js"),
  },
  {
    step: 33,
    name: "ask_a_question",
    description:
      "Query in answer mode: the request, and reading answer, sources, citations, run ID, status",
    load: () => import("./workplace/ask_a_question.js"),
  },
  {
    step: 34,
    name: "retrieve_sources_only",
    description: "Query in retrieval-only mode: the evidence without an answer",
    load: () => import("./workplace/retrieve_sources_only.js"),
  },
  {
    step: 35,
    name: "stream_an_answer",
    description:
      "Stream a query answer as it is written; events, errors, timeouts",
    load: () => import("./workplace/stream_an_answer.js"),
  },
  {
    step: 36,
    name: "answer_with_memory",
    description: "Memory modes: STANDARD versus AGENTIC, and when to use each",
    load: () => import("./workplace/answer_with_memory.js"),
  },
  {
    step: 37,
    name: "query_with_filters",
    description: "Answer from one source and documents that pass a filter",
    load: () => import("./workplace/query_with_filters.js"),
  },
  {
    step: 38,
    name: "chat_with_follow_up_questions",
    description: "Conversations: reuse the session for follow-ups",
    load: () => import("./workplace/chat_with_follow_up_questions.js"),
  },
  {
    step: 39,
    name: "stream_chat_answer",
    description: "Stream a chat answer",
    load: () => import("./workplace/stream_chat_answer.js"),
  },
  {
    step: 40,
    name: "register_users",
    description:
      "Register users from your system, linked to your identity provider",
    load: () => import("./users/register_users.js"),
  },
  {
    step: 41,
    name: "register_users_with_workspace_access",
    description: "Register a user and give workspace and source access",
    load: () => import("./users/register_users_with_workspace_access.js"),
  },
  {
    step: 42,
    name: "search_workplace_as_user",
    description: "Act as a user from the user's own access token, and search",
    load: () => import("./workplace/search_workplace_as_user.js"),
  },
  {
    step: 43,
    name: "create_user_assertion",
    description:
      "Build and sign the user assertion your backend creates (JWT, RS256), and the public key set (JWKS) to publish; optionally sign in with it",
    load: () => import("./users/create_user_assertion.js"),
  },
  {
    step: 44,
    name: "sign_in_with_your_identity_provider",
    description:
      "Act as a user who signed in to your identity provider; search and ask a question as them",
    load: () => import("./workplace/sign_in_with_your_identity_provider.js"),
  },
] as const;

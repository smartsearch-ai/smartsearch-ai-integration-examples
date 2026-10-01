"""Facet counts and distinct counts, then drill down.

*Facets* are the "Language: English (120), French (14)" lists beside search results.
``TermsAgg(name, field, size)`` asks for the ``size`` most common values of a field among the
matching documents, each with a count. ``CardinalityAgg(name, field)`` asks how many distinct
values there are (an approximate count). The ``name`` you choose is the key under which the
answer comes back in ``result.result["aggregations"]``.

Precondition: a project WITHOUT document security. *Document security* means each document
carries access rules and every caller sees only the documents they are allowed to see. On such
projects the server refuses aggregations (HTTP 503 "Canonical retrieval denied"), because counts
could reveal documents the caller cannot see. Ask your administrator whether your project uses
document security.

Run: ./run.sh facet_counts war
"""

from smartsearch_ai import CardinalityAgg, Filter, SearchQuery, TermsAgg

from examples._common import connect, print_hits, project_id, query_text, run


def main(args: list[str]) -> None:
    q = query_text(args, "war")
    with connect() as ss:
        faceted = SearchQuery(
            q,
            response_fields=["title"],
            size=1,                                                       # hits are not the point here
            aggs=[TermsAgg("languages", "original_language", 5),          # top 5 values with counts
                  CardinalityAgg("distinct_languages", "original_language")],   # how many different values
        )
        # POST {api_url}/core/projects/{project_id}/search
        aggs = ss.search().search(project_id(), faceted).result.get("aggregations", {})
        # aggregations = { "languages": { "buckets": [ {"key": "en", "doc_count": 120}, ... ] },
        #                  "distinct_languages": { "value": 17 } }
        print(f"distinct languages: {aggs.get('distinct_languages', {}).get('value', '?')}")
        buckets = aggs.get("languages", {}).get("buckets", [])
        for b in buckets:
            print(f"  {b.get('key')} ({b.get('doc_count')})")
        if not buckets:
            return
        # Drill down: the user clicks the first facet value, which becomes a filter.
        language = str(buckets[0]["key"])
        print(f"Drill down to original_language={language}:")
        print_hits(ss.search().search(project_id(), SearchQuery(
            q, size=3, response_fields=["title", "original_language"],
            filters=[Filter.term("original_language", language)])), "title", "original_language")


if __name__ == "__main__":
    run(main)

"""Highlighting: show the user WHY a result matched, with the query words marked.

With ``highlight=True`` each hit gets a ``highlight`` object: for each matching field, a list of
short text fragments in which the matched words are wrapped in ``<em>...</em>``. The keys are
internal names for the analysed versions of your fields; you do not need them, only the
fragments. The same field can appear under more than one key, so de-duplicate the fragments.

The fragments are HTML-escaped text plus ``<em>`` tags. Render them as HTML only after checking
that ``<em>`` is the only markup, or replace the tags with your own styling.

Precondition: as ``first_search``. Run: ./run.sh highlight_matches princess
"""

from smartsearch_ai import SearchQuery

from examples._common import connect, hits_of, project_id, query_text, run, shorten


def main(args: list[str]) -> None:
    with connect() as ss:
        query = SearchQuery(
            query_text(args, "princess"),
            response_fields=["title"],
            highlight=True,                 # add matching fragments to each hit
            size=3,
        )
        # POST {api_url}/core/projects/{project_id}/search
        for hit in hits_of(ss.search().search(project_id(), query)):
            print("- " + str(hit.get("_source", {}).get("title")))
            # highlight = { "<field key>": ["fragment with <em>word</em>", ...], ... }
            fragments = dict.fromkeys(                  # an ordered set: de-duplicates, keeps order
                shorten(f, 120) for field in hit.get("highlight", {}).values() for f in field if "<em>" in f)
            for fragment in list(fragments)[:3]:
                print("    " + fragment)


if __name__ == "__main__":
    run(main)

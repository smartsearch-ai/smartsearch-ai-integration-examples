# Step 21: facet_counts
# HTTP: POST /core/projects/{projectId}/search
# Problem: Show language counts next to results and turn a selected facet into a filter.
# Run: ./run.sh facet_counts [query]
# Needs: assigned SMARTSEARCH_PROJECT_ID; Movies business-field schema.
# Expect: request route/JSON, received hits, actual mode and warning.
# Try: predict the change, run it, then change one option or filter value.
# If refused: verify project assignment and field names; zero hits can be valid.
# Requires a project WITHOUT document security; secured projects refuse facets.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# Repeated calls below change the demonstrated option or conversation turn.
# {
#   "from": 0,
#   "size": 5,
#   "response_fields": [
#     "title"
#   ],
#   "aggs": [
#     {
#       "type": "terms",
#       "name": "languages",
#       "field": "original_language",
#       "size": 10
#     },
#     {
#       "type": "cardinality",
#       "name": "distinct_languages",
#       "field": "original_language"
#     }
#   ],
#   "q": "war"
# }
# End request JSON.

q="${*:-war}"
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title"
  ],
  "aggs": [
    {
      "type": "terms",
      "name": "languages",
      "field": "original_language",
      "size": 10
    },
    {
      "type": "cardinality",
      "name": "distinct_languages",
      "field": "original_language"
    }
  ]
} + {
  q: $q
}
')"
jq .result.aggregations "$_BODY"
language=$(jq -r '.result.aggregations.languages.buckets[0].key // empty' "$_BODY")
if [ -n "$language" ]; then
  rest_project_search "$(jq -n --arg q "$q" --arg language "$language" '
{
  q: $q,
  from: 0,
  size: 5,
  response_fields: [
    "title"
  ],
  filters: {
    all: [
      {
        search_type: "term",
        field: "original_language",
        value: $language
      }
    ]
  }
}
')"
fi

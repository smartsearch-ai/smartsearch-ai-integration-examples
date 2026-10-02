# Step 12: filter_by_exact_phrase
# HTTP: POST /core/projects/{projectId}/search
# Problem: Words in a tagline must appear together in the same order.
# Run: ./run.sh filter_by_exact_phrase [query]
# Needs: assigned SMARTSEARCH_PROJECT_ID; Movies business-field schema.
# Expect: request route/JSON, received hits, actual mode and warning.
# Try: predict the change, run it, then change one option or filter value.
# If refused: verify project assignment and field names; zero hits can be valid.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# {
#   "q": "galaxy",
#   "from": 0,
#   "size": 5,
#   "response_fields": [
#     "title",
#     "tagline"
#   ],
#   "filters": {
#     "all": [
#       {
#         "search_type": "match_phrase",
#         "field": "tagline",
#         "value": "a galaxy far, far away"
#       }
#     ]
#   }
# }
# End request JSON.

phrase="${*:-a galaxy far, far away}"
rest_project_search "$(jq -n --arg phrase "$phrase" '
{
  q: "galaxy",
  from: 0,
  size: 5,
  response_fields: [
    "title",
    "tagline"
  ],
  filters: {
    all: [
      {
        search_type: "match_phrase",
        field: "tagline",
        value: $phrase
      }
    ]
  }
}
')"

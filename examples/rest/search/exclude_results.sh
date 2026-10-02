# Step 13: exclude_results
# HTTP: POST /core/projects/{projectId}/search
# Problem: Users want relevant movies while excluding one genre.
# Run: ./run.sh exclude_results [query]
# Needs: assigned SMARTSEARCH_PROJECT_ID; Movies business-field schema.
# Expect: request route/JSON, received hits, actual mode and warning.
# Try: predict the change, run it, then change one option or filter value.
# If refused: verify project assignment and field names; zero hits can be valid.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# {
#   "from": 0,
#   "size": 5,
#   "response_fields": [
#     "title"
#   ],
#   "filters": {
#     "not": [
#       {
#         "search_type": "term",
#         "field": "genres.name",
#         "value": "Horror"
#       }
#     ]
#   },
#   "q": "love"
# }
# End request JSON.

q="${*:-love}"
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title"
  ],
  "filters": {
    "not": [
      {
        "search_type": "term",
        "field": "genres.name",
        "value": "Horror"
      }
    ]
  }
} + {
  q: $q
}
')"

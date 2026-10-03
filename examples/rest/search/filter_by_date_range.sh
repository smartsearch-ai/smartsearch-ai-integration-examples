# Step 9: filter_by_date_range
# HTTP: POST /core/projects/{projectId}/search
# Problem: A year picker should keep movies released during one year.
# Run: ./run.sh filter_by_date_range [query]
# Needs: assigned SMARTSEARCH_PROJECT_ID; Movies business-field schema.
# Expect: request route/JSON, received hits, actual mode and warning.
# Try: predict the change, run it, then change one option or filter value.
# If refused: verify project assignment and field names; zero hits can be valid.
# Movies release_date accepts whole-year bounds here; ask your administrator about your schema.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# {
#   "from": 0,
#   "size": 5,
#   "response_fields": [
#     "title",
#     "release_date"
#   ],
#   "filters": {
#     "all": [
#       {
#         "search_type": "range",
#         "field": "release_date",
#         "condition": {
#           "gte": 2000,
#           "lt": 2001
#         }
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
    "title",
    "release_date"
  ],
  "filters": {
    "all": [
      {
        "search_type": "range",
        "field": "release_date",
        "condition": {
          "gte": 2000,
          "lt": 2001
        }
      }
    ]
  }
} + {
  q: $q
}
')"

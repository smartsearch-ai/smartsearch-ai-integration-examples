# Step 10: filter_where_field_exists
# HTTP: POST /core/projects/{projectId}/search
# Problem: Do not show movies that lack a tagline.
# Run: ./run.sh filter_where_field_exists [query]
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
#     "all": [
#       {
#         "search_type": "exists",
#         "field": "tagline"
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
    "all": [
      {
        "search_type": "exists",
        "field": "tagline"
      }
    ]
  }
} + {
  q: $q
}
')"

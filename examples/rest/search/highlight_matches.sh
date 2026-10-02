# Step 16: highlight_matches
# HTTP: POST /core/projects/{projectId}/search
# Problem: Your results page should explain which words matched.
# Run: ./run.sh highlight_matches [query]
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
#   "ssapi_flags": {
#     "highlight_enabled": true
#   },
#   "q": "princess"
# }
# End request JSON.

q="${*:-princess}"
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title"
  ],
  "ssapi_flags": {
    "highlight_enabled": true
  }
} + {
  q: $q
}
')"

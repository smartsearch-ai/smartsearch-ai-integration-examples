# Step 2: first_search
# HTTP: POST /core/projects/{projectId}/search
# Problem: You need the first results page for a Movies project.
# Run: ./run.sh first_search [query]
# Needs: assigned SMARTSEARCH_PROJECT_ID; Movies business-field schema.
# Expect: request route/JSON, received hits, actual mode and warning.
# Try: change size from 5 to 2 and count the returned hits.
# If refused: verify project assignment and field names; zero hits can be valid.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# {
#   "from": 0,
#   "size": 5,
#   "response_fields": [
#     "title",
#     "release_date",
#     "vote_average"
#   ],
#   "q": "star wars"
# }
# End request JSON.

q="${*:-star wars}"
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title",
    "release_date",
    "vote_average"
  ]
} + {
  q: $q
}
')"

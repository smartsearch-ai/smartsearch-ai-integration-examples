# Step 3: choose_searched_and_returned_fields
# HTTP: POST /core/projects/{projectId}/search
# Problem: Users search long descriptions, but your results page only needs a title and tagline.
# Run: ./run.sh choose_searched_and_returned_fields [query]
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
#     "title",
#     "tagline"
#   ],
#   "fields": [
#     "title",
#     "overview"
#   ],
#   "q": "galaxy"
# }
# End request JSON.

q="${*:-galaxy}"
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title",
    "tagline"
  ],
  "fields": [
    "title",
    "overview"
  ]
} + {
  q: $q
}
')"

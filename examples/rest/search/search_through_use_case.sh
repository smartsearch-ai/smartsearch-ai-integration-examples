# Step 29: search_through_use_case
# HTTP: POST /core/usecases/{usecaseId}/search
# Problem: Reuse a saved search configuration rather than choosing its options on every request.
# Run: ./run.sh search_through_use_case [query]
# Needs: assigned SMARTSEARCH_USECASE_ID; Movies business-field schema.
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
#   "q": "star wars"
# }
# End request JSON.

q="${*:-star wars}"
rest_usecase_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title"
  ]
} + {
  q: $q
}
')"

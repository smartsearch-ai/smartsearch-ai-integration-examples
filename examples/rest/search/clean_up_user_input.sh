# Step 19: clean_up_user_input
# HTTP: POST /core/projects/{projectId}/search
# Problem: Typed input contains extra spacing or punctuation; ask the search pipeline to normalize it.
# Run: ./run.sh clean_up_user_input [query]
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
#     "trim_query": true,
#     "remove_special_chars": true
#   },
#   "q": "   love !!!   "
# }
# End request JSON.

q="${*:-   love !!!   }"
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title"
  ],
  "ssapi_flags": {
    "trim_query": true,
    "remove_special_chars": true
  }
} + {
  q: $q
}
')"

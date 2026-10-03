# Step 30: handle_errors_and_warnings
# HTTP: POST /core/projects/{projectId}/search
# Problem: Distinguish invalid input, server refusal and a successful search that used a different technique.
# Run: ./run.sh handle_errors_and_warnings [query]
# Needs: assigned SMARTSEARCH_PROJECT_ID; Movies business-field schema.
# Expect: request route/JSON, received hits, actual mode and warning.
# Try: predict the change, run it, then change one option or filter value.
# If refused: verify project assignment and field names; zero hits can be valid.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# Repeated calls below change the demonstrated option or conversation turn.
# {
#   "q": "love",
#   "from": 0,
#   "size": 5
# }
# End request JSON.

q="${*:-star wars}"
# Local validation: precision is 1..11, so do not send an invalid value.
echo '1. precision=12 is outside the public 1..11 contract; request not sent.'
project=$(_segment "$(_required SMARTSEARCH_PROJECT_ID)")
api=$(_url "$(_required SMARTSEARCH_API_BASE_URL)")
echo '2. Expected refusal for an unknown project:'
if _request "$api" '/core/projects/no-such-project/search' POST '{"q":"love","from":0,"size":5}' && _core_success; then
  echo 'Unexpected success: ask the administrator about the fixture project.'
fi
echo '3. Requested technique versus actual mode/warning:'
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title"
  ],
  "ssapi_flags": {
    "neural_mode": "EXACT_AND_BM25_FUSED"
  }
} + {
  q: $q
}
')"

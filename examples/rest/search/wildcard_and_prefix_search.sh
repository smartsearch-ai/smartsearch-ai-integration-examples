# Step 17: wildcard_and_prefix_search
# HTTP: POST /core/projects/{projectId}/search
# Problem: Users enter a prefix such as avat*; compare wildcard matching with ordinary matching.
# Run: ./run.sh wildcard_and_prefix_search [query]
# Needs: assigned SMARTSEARCH_PROJECT_ID; Movies business-field schema.
# Expect: request route/JSON, received hits, actual mode and warning.
# Try: predict the change, run it, then change one option or filter value.
# If refused: verify project assignment and field names; zero hits can be valid.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# Repeated calls below change the demonstrated option or conversation turn.
# {
#   "from": 0,
#   "size": 5,
#   "response_fields": [
#     "title"
#   ],
#   "ssapi_flags": {
#     "wild_card_search": true,
#     "neural_mode": "BM25"
#   },
#   "q": "avat*"
# }
# End request JSON.

q="${*:-avat*}"
echo "Comparison 1: inspect the request and its result."
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title"
  ],
  "ssapi_flags": {
    "wild_card_search": true,
    "neural_mode": "BM25"
  }
} + {
  q: $q
}
')"
echo "Comparison 2: inspect the request and its result."
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title"
  ],
  "ssapi_flags": {
    "wild_card_search": false,
    "neural_mode": "BM25"
  }
} + {
  q: $q
}
')"

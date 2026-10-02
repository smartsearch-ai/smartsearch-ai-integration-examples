# Step 4: page_through_results
# HTTP: POST /core/projects/{projectId}/search
# Problem: Your results page needs a second and third page without reranking ignoring the offset.
# Run: ./run.sh page_through_results [query]
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
#     "neural_mode": "BM25",
#     "rerank_enabled": false
#   },
#   "q": "love"
# }
# End request JSON.

q="${*:-love}"
echo "Comparison 1: inspect the request and its result."
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title"
  ],
  "ssapi_flags": {
    "neural_mode": "BM25",
    "rerank_enabled": false
  }
} + {
  q: $q
}
')"
echo "Comparison 2: inspect the request and its result."
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 5,
  "size": 5,
  "response_fields": [
    "title"
  ],
  "ssapi_flags": {
    "neural_mode": "BM25",
    "rerank_enabled": false
  }
} + {
  q: $q
}
')"
echo "Comparison 3: inspect the request and its result."
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 10,
  "size": 5,
  "response_fields": [
    "title"
  ],
  "ssapi_flags": {
    "neural_mode": "BM25",
    "rerank_enabled": false
  }
} + {
  q: $q
}
')"

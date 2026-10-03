# Step 24: limit_semantic_matches
# HTTP: POST /core/projects/{projectId}/search
# Problem: Balance semantic candidate depth against the number of closest matches you keep.
# Run: ./run.sh limit_semantic_matches [query]
# Needs: assigned SMARTSEARCH_PROJECT_ID; Movies business-field schema.
# Expect: request route/JSON, received hits, actual mode and warning.
# Try: predict the change, run it, then change one option or filter value.
# If refused: verify project assignment and field names; zero hits can be valid.
# Needs project embeddings for vector/hybrid retrieval; inspect actual mode and warning.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# Repeated calls below change the demonstrated option or conversation turn.
# {
#   "from": 0,
#   "size": 10,
#   "response_fields": [
#     "title"
#   ],
#   "ssapi_flags": {
#     "neural_mode": "A_KNN",
#     "neural_top_matches": 3,
#     "neural_total_matches": 100
#   },
#   "q": "a toy cowboy afraid of being replaced"
# }
# End request JSON.

q="${*:-a toy cowboy afraid of being replaced}"
echo "Comparison 1: inspect the request and its result."
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 10,
  "response_fields": [
    "title"
  ],
  "ssapi_flags": {
    "neural_mode": "A_KNN",
    "neural_top_matches": 3,
    "neural_total_matches": 100
  }
} + {
  q: $q
}
')"
echo "Comparison 2: inspect the request and its result."
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 10,
  "response_fields": [
    "title"
  ],
  "ssapi_flags": {
    "neural_mode": "A_KNN",
    "neural_top_matches": 10,
    "neural_total_matches": 100
  }
} + {
  q: $q
}
')"

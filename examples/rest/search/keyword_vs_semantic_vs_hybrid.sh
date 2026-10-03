# Step 22: keyword_vs_semantic_vs_hybrid
# HTTP: POST /core/projects/{projectId}/search
# Problem: Does exact wording or meaning work better for this query? Compare keyword, vector and hybrid retrieval.
# Run: ./run.sh keyword_vs_semantic_vs_hybrid [query]
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
#   "size": 5,
#   "response_fields": [
#     "title"
#   ],
#   "ssapi_flags": {
#     "neural_mode": "BM25"
#   },
#   "q": "rebels fight an evil empire in space"
# }
# End request JSON.

q="${*:-rebels fight an evil empire in space}"
echo "Comparison 1: inspect the request and its result."
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title"
  ],
  "ssapi_flags": {
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
    "neural_mode": "A_KNN"
  }
} + {
  q: $q
}
')"
echo "Comparison 3: inspect the request and its result."
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title"
  ],
  "ssapi_flags": {
    "neural_mode": "A_KNN_AND_BM25"
  }
} + {
  q: $q
}
')"

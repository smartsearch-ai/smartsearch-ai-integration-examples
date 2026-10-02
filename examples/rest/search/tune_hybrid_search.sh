# Step 23: tune_hybrid_search
# HTTP: POST /core/projects/{projectId}/search
# Problem: Two retrieval lists need combining; compare rank fusion with normalized score fusion.
# Run: ./run.sh tune_hybrid_search [query]
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
#     "neural_mode": "A_KNN_AND_BM25",
#     "normalization_technique": "RANK",
#     "normalization_combination_technique": "RRF",
#     "neural_rank_window_size": 50,
#     "neural_rank_constant": 60
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
    "neural_mode": "A_KNN_AND_BM25",
    "normalization_technique": "RANK",
    "normalization_combination_technique": "RRF",
    "neural_rank_window_size": 50,
    "neural_rank_constant": 60
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
    "neural_mode": "A_KNN_AND_BM25",
    "normalization_technique": "MIN_MAX",
    "normalization_combination_technique": "ARITHMETIC_MEAN"
  }
} + {
  q: $q
}
')"

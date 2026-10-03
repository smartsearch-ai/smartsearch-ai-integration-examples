# Step 26: tune_reranking
# HTTP: POST /core/projects/{projectId}/search
# Problem: Control how many candidates the model rescores and whether low-scoring matches survive.
# Run: ./run.sh tune_reranking [query]
# Needs: assigned SMARTSEARCH_PROJECT_ID; Movies business-field schema.
# Expect: request route/JSON, received hits, actual mode and warning.
# Try: predict the change, run it, then change one option or filter value.
# If refused: verify project assignment and field names; zero hits can be valid.
# Needs a configured reranker for the enabled comparison.

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
#     "rerank_enabled": true,
#     "rerank_first_stage_size": 10
#   },
#   "q": "wizard school"
# }
# End request JSON.

q="${*:-wizard school}"
echo "Comparison 1: inspect the request and its result."
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title"
  ],
  "ssapi_flags": {
    "rerank_enabled": true,
    "rerank_first_stage_size": 10
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
    "rerank_enabled": true,
    "rerank_first_stage_size": 50
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
    "rerank_enabled": true,
    "rerank_first_stage_size": 30,
    "rerank_min_score": 0.5
  }
} + {
  q: $q
}
')"

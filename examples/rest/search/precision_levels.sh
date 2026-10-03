# Step 27: precision_levels
# HTTP: POST /core/projects/{projectId}/search
# Problem: Compare broad and strict keyword matching without changing the query.
# Run: ./run.sh precision_levels [query]
# Needs: assigned SMARTSEARCH_PROJECT_ID; Movies business-field schema.
# Expect: request route/JSON, received hits, actual mode and warning.
# Try: predict the change, run it, then change one option or filter value.
# If refused: verify project assignment and field names; zero hits can be valid.
# Needs a precision template on the project; request flags cannot install one.

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
#     "precision": 1
#   },
#   "q": "dark knight rises"
# }
# End request JSON.

q="${*:-dark knight rises}"
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
    "precision": 1
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
    "neural_mode": "BM25",
    "precision": 6
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
    "neural_mode": "BM25",
    "precision": 11
  }
} + {
  q: $q
}
')"

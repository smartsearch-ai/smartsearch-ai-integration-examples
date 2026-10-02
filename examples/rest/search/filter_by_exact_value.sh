# Step 6: filter_by_exact_value
# HTTP: POST /core/projects/{projectId}/search
# Problem: Users selected a language or genre; keep only documents with that exact value.
# Run: ./run.sh filter_by_exact_value [query]
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
#   "filters": {
#     "all": [
#       {
#         "search_type": "term",
#         "field": "original_language",
#         "value": "fr"
#       }
#     ]
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
  "filters": {
    "all": [
      {
        "search_type": "term",
        "field": "original_language",
        "value": "fr"
      }
    ]
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
  "filters": {
    "all": [
      {
        "search_type": "term",
        "field": "genres.name",
        "value": "Comedy"
      }
    ]
  }
} + {
  q: $q
}
')"

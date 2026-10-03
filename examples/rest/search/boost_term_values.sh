# Step 15: boost_term_values
# HTTP: POST /core/projects/{projectId}/search
# Problem: Prefer a language without excluding relevant movies in other languages.
# Run: ./run.sh boost_term_values [query]
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
#     "rerank_enabled": false
#   },
#   "boosts": [
#     {
#       "search_type": "term",
#       "field": "original_language",
#       "value": "en",
#       "weight": 3
#     }
#   ],
#   "q": "love"
# }
# End request JSON.

q="${*:-love}"
rest_project_search "$(jq -n --arg q "$q" '
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title"
  ],
  "ssapi_flags": {
    "rerank_enabled": false
  },
  "boosts": [
    {
      "search_type": "term",
      "field": "original_language",
      "value": "en",
      "weight": 3
    }
  ]
} + {
  q: $q
}
')"

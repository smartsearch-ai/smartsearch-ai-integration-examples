# Step 11: filter_by_full_text_match
# HTTP: POST /core/projects/{projectId}/search
# Problem: The query matches broadly, but each movie overview must also mention a chosen word.
# Run: ./run.sh filter_by_full_text_match "love" paris
# Needs: assigned SMARTSEARCH_PROJECT_ID; Movies business-field schema.
# Expect: request route/JSON, received hits, actual mode and warning.
# Try: predict the change, run it, then change one option or filter value.
# If refused: verify project assignment and field names; zero hits can be valid.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# {
#   "q": "love",
#   "from": 0,
#   "size": 5,
#   "response_fields": [
#     "title",
#     "overview"
#   ],
#   "filters": {
#     "all": [
#       {
#         "search_type": "match",
#         "field": "overview",
#         "value": "paris"
#       }
#     ]
#   }
# }
# End request JSON.

q="${1:-love}"
word="${2:-paris}"
# q finds relevant movies; the overview must also mention word.
rest_project_search "$(jq -n --arg q "$q" --arg word "$word" '
{
  q: $q,
  from: 0,
  size: 5,
  response_fields: [
    "title",
    "overview"
  ],
  filters: {
    all: [
      {
        search_type: "match",
        field: "overview",
        value: $word
      }
    ]
  }
}
')"

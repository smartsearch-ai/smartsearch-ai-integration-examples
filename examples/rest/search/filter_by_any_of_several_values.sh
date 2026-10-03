# Step 7: filter_by_any_of_several_values
# HTTP: POST /core/projects/{projectId}/search
# Problem: Users selected two genres; either genre should qualify.
# Run: ./run.sh filter_by_any_of_several_values [query]
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
#   "filters": {
#     "all": [
#       {
#         "any": [
#           {
#             "search_type": "term",
#             "field": "genres.name",
#             "value": "Horror"
#           },
#           {
#             "search_type": "term",
#             "field": "genres.name",
#             "value": "Animation"
#           }
#         ]
#       }
#     ]
#   },
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
  "filters": {
    "all": [
      {
        "any": [
          {
            "search_type": "term",
            "field": "genres.name",
            "value": "Horror"
          },
          {
            "search_type": "term",
            "field": "genres.name",
            "value": "Animation"
          }
        ]
      }
    ]
  }
} + {
  q: $q
}
')"

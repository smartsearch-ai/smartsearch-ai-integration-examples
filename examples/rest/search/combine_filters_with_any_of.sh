# Step 14: combine_filters_with_any_of
# HTTP: POST /core/projects/{projectId}/search
# Problem: Require released movies and either genre, then exclude a language.
# Run: ./run.sh combine_filters_with_any_of [query]
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
#         "search_type": "term",
#         "field": "status",
#         "value": "Released"
#       },
#       {
#         "any": [
#           {
#             "search_type": "term",
#             "field": "genres.name",
#             "value": "Comedy"
#           },
#           {
#             "search_type": "term",
#             "field": "genres.name",
#             "value": "Animation"
#           }
#         ]
#       }
#     ],
#     "not": [
#       {
#         "search_type": "term",
#         "field": "original_language",
#         "value": "fr"
#       }
#     ]
#   },
#   "q": "space"
# }
# End request JSON.

q="${*:-space}"
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
        "field": "status",
        "value": "Released"
      },
      {
        "any": [
          {
            "search_type": "term",
            "field": "genres.name",
            "value": "Comedy"
          },
          {
            "search_type": "term",
            "field": "genres.name",
            "value": "Animation"
          }
        ]
      }
    ],
    "not": [
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

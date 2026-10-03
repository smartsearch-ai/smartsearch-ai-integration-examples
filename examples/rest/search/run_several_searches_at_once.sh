# Step 28: run_several_searches_at_once
# HTTP: POST /core/projects/{projectId}/mSearch
# Problem: Your page needs several independent result lists in one HTTP round trip.
# Run: ./run.sh run_several_searches_at_once [query]
# Needs: assigned SMARTSEARCH_PROJECT_ID; Movies business-field schema.
# Expect: request route/JSON, per-query received counts and up to five shown hits.
# Try: predict the change, run it, then change one option or filter value.
# If refused: verify project assignment and field names; zero hits can be valid.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# [
#   {
#     "q": "alien",
#     "from": 0,
#     "size": 5,
#     "response_fields": [
#       "title"
#     ]
#   },
#   {
#     "q": "titanic",
#     "from": 0,
#     "size": 5,
#     "response_fields": [
#       "title"
#     ]
#   },
#   {
#     "q": "toy story",
#     "from": 0,
#     "size": 5,
#     "response_fields": [
#       "title"
#     ]
#   }
# ]
# End request JSON.

# mSearch receives a bare JSON array, not {queries:[...]}.
if [ "$#" = 0 ]; then set -- alien titanic "toy story"; fi
queries=$(printf '%s\n' "$@" | jq -Rsc 'split("\n")[:-1]|map({q:.,from:0,size:5,response_fields:["title"]})')
rest_multi_search "$queries"

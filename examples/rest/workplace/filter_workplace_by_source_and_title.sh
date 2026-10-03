# Step 32: filter_workplace_by_source_and_title
# HTTP: POST /workspace/v1/workspaces/{workspaceId}/search
# Problem: Search one allowed source and only documents whose title mentions a word.
# Run: ./run.sh filter_workplace_by_source_and_title "travel policy" policy
# Needs: SMARTSEARCH_WORKSPACE_ID and caller membership; no answering agent is needed for search.
# Expect: a documents array containing only permitted search results.
# Try: ask one question, then change only the source, mode or conversation input.
# If refused: check workspace membership, sources and Act as users when delegated.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# {
#   "query": "What is our travel policy?",
#   "mode": "retrieval_only",
#   "memory_mode": "standard",
#   "source_ids": [
#     "demo-source"
#   ],
#   "filters": {
#     "all": [
#       {
#         "search_type": "match",
#         "field": "title",
#         "value": "policy"
#       }
#     ]
#   }
# }
# End request JSON.

q="${1:-What is our travel policy?}"
word="${2:-policy}"
source=$(_required SMARTSEARCH_SOURCE_ID)
# Source selection narrows retrieval; it never grants access to otherwise forbidden documents.
rest_workspace search "$(jq -n --arg q "$q" --arg source "$source" --arg word "$word" '
{
  query: $q,
  mode: "retrieval_only",
  memory_mode: "standard",
  source_ids: [
    $source
  ],
  filters: {
    all: [
      {
        search_type: "match",
        field: "title",
        value: $word
      }
    ]
  }
}
')"

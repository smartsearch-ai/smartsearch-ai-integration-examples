# Step 37: query_with_filters
# HTTP: POST /workspace/v1/workspaces/{workspaceId}/query
# Problem: An answer must use evidence from one source and a matching title.
# Run: ./run.sh query_with_filters "travel policy" policy
# Needs: SMARTSEARCH_WORKSPACE_ID and caller membership; answer mode needs an agent.
# Expect: a grounded answer with sources and final run status.
# Try: ask one question, then change only the source, mode or conversation input.
# If refused: check workspace membership, sources and Act as users when delegated.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# {
#   "query": "What is our travel policy?",
#   "mode": "answer",
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
rest_workspace query "$(jq -n --arg q "$q" --arg source "$source" --arg word "$word" '
{
  query: $q,
  mode: "answer",
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

# Step 31: search_workplace_as_service
# HTTP: POST /workspace/v1/workspaces/{workspaceId}/search
# Problem: Your backend needs permitted Workplace documents using its own service identity.
# Run: ./run.sh search_workplace_as_service [query]
# Needs: SMARTSEARCH_WORKSPACE_ID and caller membership; no answering agent is needed for search.
# Expect: a documents array containing only permitted search results.
# Try: ask one question, then change only the source, mode or conversation input.
# If refused: check workspace membership, sources and Act as users when delegated.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# {
#   "query": "What is our travel policy?",
#   "mode": "retrieval_only",
#   "memory_mode": "standard"
# }
# End request JSON.

q="${*:-What is our travel policy?}"
rest_workspace search "$(jq -n --arg q "$q" '
{
  query: $q,
  mode: "retrieval_only",
  memory_mode: "standard"
}
')"

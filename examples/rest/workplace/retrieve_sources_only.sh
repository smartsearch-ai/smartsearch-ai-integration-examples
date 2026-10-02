# Step 34: retrieve_sources_only
# HTTP: POST /workspace/v1/workspaces/{workspaceId}/query
# Problem: Your own model needs the permitted evidence, without asking Workplace to generate an answer.
# Run: ./run.sh retrieve_sources_only [query]
# Needs: SMARTSEARCH_WORKSPACE_ID and caller membership; no answering agent is needed.
# Expect: evidence in sources, with no generated answer.
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
rest_workspace query "$(jq -n --arg q "$q" '
{
  query: $q,
  mode: "retrieval_only",
  memory_mode: "standard"
}
')"

# Step 33: ask_a_question
# HTTP: POST /workspace/v1/workspaces/{workspaceId}/query
# Problem: Return one grounded answer with its evidence instead of only a results list.
# Run: ./run.sh ask_a_question [query]
# Needs: SMARTSEARCH_WORKSPACE_ID and caller membership; answer mode needs an agent.
# Expect: a grounded answer with sources and final run status.
# Try: ask one question, then change only the source, mode or conversation input.
# If refused: check workspace membership, sources and Act as users when delegated.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# {
#   "query": "What is our travel policy?",
#   "mode": "answer",
#   "memory_mode": "standard"
# }
# End request JSON.

q="${*:-What is our travel policy?}"
rest_workspace query "$(jq -n --arg q "$q" '
{
  query: $q,
  mode: "answer",
  memory_mode: "standard"
}
')"

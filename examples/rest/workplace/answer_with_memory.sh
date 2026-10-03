# Step 36: answer_with_memory
# HTTP: POST /workspace/v1/workspaces/{workspaceId}/query
# Problem: Compare a one-off answer with an answer that records context in the caller memory.
# Run: ./run.sh answer_with_memory [query]
# Needs: SMARTSEARCH_WORKSPACE_ID and caller membership; answer mode needs an agent.
# Expect: a grounded answer with sources and final run status.
# Try: ask one question, then change only the source, mode or conversation input.
# If refused: check workspace membership, sources and Act as users when delegated.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# Repeated calls below change the demonstrated option or conversation turn.
# {
#   "query": "What is our travel policy?",
#   "mode": "answer",
#   "memory_mode": "standard"
# }
# End request JSON.

q="${*:-What is our travel policy?}"
# Agentic records a question in the SERVICE identity memory; many people would share it.
for memory in standard agentic; do
  rest_workspace query "$(jq -n --arg q "$q" --arg memory "$memory" '
{
  query: $q,
  mode: "answer",
  memory_mode: $memory
}
')"
done

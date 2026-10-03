# Step 38: chat_with_follow_up_questions
# HTTP: POST /workspace/v1/workspaces/{workspaceId}/chat
# Problem: A follow-up refers to the first question; preserve the returned conversation session.
# Run: ./run.sh chat_with_follow_up_questions [query]
# Needs: SMARTSEARCH_WORKSPACE_ID and caller membership; answer mode needs an agent.
# Expect: permitted documents or evidence/answer, according to the chosen operation.
# Try: ask one question, then change only the source, mode or conversation input.
# If refused: check workspace membership, sources and Act as users when delegated.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# Repeated calls below change the demonstrated option or conversation turn.
# {
#   "query": "Who wrote our travel policy?",
#   "memory_mode": "standard"
# }
# End request JSON.

q="${1:-Who wrote our travel policy?}"
rest_workspace chat "$(jq -n --arg q "$q" '
{
  query: $q,
  memory_mode: "standard"
}
')"
session=$(jq -er '.session_id|select(type=="string" and length>0)' "$_BODY")
q="${2:-When was it last updated?}"
# Reuse session_id, not the whole prior response, to preserve this conversation.
rest_workspace chat "$(jq -n --arg q "$q" --arg session "$session" '
{
  query: $q,
  session_id: $session,
  memory_mode: "standard"
}
')"

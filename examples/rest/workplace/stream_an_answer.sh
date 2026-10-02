# Step 35: stream_an_answer
# HTTP: POST /workspace/v1/workspaces/{workspaceId}/query
# Problem: Show answer text as it arrives, then use the final completion as the authoritative answer.
# Run: ./run.sh stream_an_answer [query]
# Needs: SMARTSEARCH_WORKSPACE_ID and caller membership; answer mode needs an agent.
# Expect: provisional answer fragments, then a pretty final COMPLETED result; fragments alone are not success.
# Try: ask one question, then change only the source, mode or conversation input.
# If refused: check workspace membership, sources and Act as users when delegated.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# {
#   "query": "What is our travel policy?",
#   "mode": "answer",
#   "memory_mode": "standard",
#   "options": {
#     "stream": true
#   }
# }
# End request JSON.

q="${*:-What is our travel policy?}"
rest_stream query "$(jq -n --arg q "$q" '
{
  query: $q,
  mode: "answer",
  memory_mode: "standard",
  options: {
    stream: true
  }
}
')"

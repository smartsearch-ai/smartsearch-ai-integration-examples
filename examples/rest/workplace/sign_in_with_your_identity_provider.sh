# Step 44: sign_in_with_your_identity_provider
# HTTP: POST /realms/{realm}/protocol/openid-connect/token (form), then POST /workspace/v1/workspaces/{workspaceId}/search
# Problem: Exchange a fresh signed identity assertion, then search and answer as that user.
# Run: ./run.sh sign_in_with_your_identity_provider [query]
# Needs: SMARTSEARCH_WORKSPACE_ID and caller membership; answer mode needs an agent.
# Expect: a grounded answer with sources and final run status.
# Try: ask one question, then change only the source, mode or conversation input.
# If refused: check workspace membership, sources and Act as users when delegated.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# Repeated calls below change the demonstrated option or conversation turn.
# {
#   "query": "What is our travel policy?",
#   "mode": "retrieval_only",
#   "memory_mode": "standard"
# }
# End request JSON.

rest_as_user assertion
q="${*:-What is our travel policy?}"
rest_workspace search "$(jq -n --arg q "$q" '
{
  query: $q,
  mode: "retrieval_only",
  memory_mode: "standard"
}
')"
rest_workspace query "$(jq -n --arg q "$q" '
{
  query: $q,
  mode: "answer",
  memory_mode: "standard"
}
')"

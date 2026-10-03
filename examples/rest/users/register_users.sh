# Step 40: register_users
# HTTP: POST /search-admin/api/provisioning/v1/jobs
# HTTP: GET /search-admin/api/provisioning/v1/jobs/{jobId}
# HTTP: GET /search-admin/api/provisioning/v1/jobs/{jobId}/items
# HTTP: GET /search-admin/api/provisioning/v1/integrations/{integrationId}/principals
# Problem: Create two users from your identity provider without managing SmartSearch passwords.
# Run: ./run.sh register_users
# Needs: Owner-configured Register users (federated mode), tenant and integration IDs.
# Expect: terminal job state and one outcome per registered user; inspect refused items.
# Try: inspect every item; a terminal job can still contain refused users.
# If refused: check tenant, registered identity provider and allowed grants.

# Request JSON:
# Complete representative payload; demo resource IDs stand for your task settings.
# {
#   "scope": {
#     "kind": "TENANT",
#     "id": "demo tenant"
#   },
#   "integration_id": "demo-integration",
#   "kind": "UPSERT_USERS",
#   "items": [
#     {
#       "item_key": "user-1",
#       "external_user_id": "sdk-example-user-1",
#       "profile": {
#         "email": "sdk-example-user-1@example.com",
#         "first_name": "Ada",
#         "last_name": "Example",
#         "display_name": "Ada Example"
#       },
#       "platform_role": "GUEST",
#       "permissions": [],
#       "workspaces": [],
#       "external_identity": {
#         "subject": "sdk-example-user-1",
#         "username": "sdk-example-user-1"
#       }
#     },
#     {
#       "item_key": "user-2",
#       "external_user_id": "sdk-example-user-2",
#       "profile": {
#         "email": "sdk-example-user-2@example.com",
#         "first_name": "Grace",
#         "last_name": "Example",
#         "display_name": "Grace Example"
#       },
#       "platform_role": "GUEST",
#       "permissions": [],
#       "workspaces": [],
#       "external_identity": {
#         "subject": "sdk-example-user-2",
#         "username": "sdk-example-user-2"
#       }
#     }
#   ]
# }
# End request JSON.

tenant=$(_required SMARTSEARCH_TENANT_ID)
integration=$(_required SMARTSEARCH_INTEGRATION_ID)
# GUEST is a platform role. Source groups/roles/security keys are document grants, not platform roles.
# External subject/username link an existing identity-provider account; email alone does not link it.
body=$(jq -n --arg tenant "$tenant" --arg integration "$integration" '
def user($n;
$first):  ("sdk-example-user-"+($n|tostring)) as $id | {
  item_key: ("user-"+($n|tostring)),
  external_user_id: $id,
  profile: {
    email: ($id+"@example.com"),
    first_name: $first,
    last_name: "Example",
    display_name: ($first+" Example")
  },
  platform_role: "GUEST",
  permissions: [
  ],
  workspaces: [
  ],
  external_identity: {
    subject: $id,
    username: $id
  }
};
{
  scope: {
    kind: "TENANT",
    id: $tenant
  },
  integration_id: $integration,
  kind: "UPSERT_USERS",
  items: [
    user(1;
    "Ada"),
    user(2;
    "Grace")
  ]
}
')
# Same idempotency key is safe ONLY with the unchanged request; new users/grants need a new key.
rest_register UPSERT_USERS sdk-example-upsert-users-v1 "$body"

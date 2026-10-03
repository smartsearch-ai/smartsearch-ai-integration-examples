# Step 41: register_users_with_workspace_access
# HTTP: POST /search-admin/api/provisioning/v1/jobs
# HTTP: GET /search-admin/api/provisioning/v1/jobs/{jobId}
# HTTP: GET /search-admin/api/provisioning/v1/jobs/{jobId}/items
# Problem: Register a user and grant workspace/source access in one idempotent onboarding job.
# Run: ./run.sh register_users_with_workspace_access
# Needs: federated Register users, tenant/integration IDs, allowed workspace/source IDs and source grants.
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
#   "kind": "ONBOARD_USERS",
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
#       "workspaces": [
#         {
#           "workspace_id": "demo-workspace",
#           "source_grants": [
#             {
#               "source_id": "demo-source",
#               "expected_revision": 0,
#               "grants": {
#                 "groups": [
#                   "everyone"
#                 ],
#                 "roles": [],
#                 "security_keys": []
#               }
#             }
#           ]
#         }
#       ],
#       "external_identity": {
#         "subject": "sdk-example-user-1",
#         "username": "sdk-example-user-1"
#       }
#     }
#   ]
# }
# End request JSON.

tenant=$(_required SMARTSEARCH_TENANT_ID)
integration=$(_required SMARTSEARCH_INTEGRATION_ID)
# GUEST is a platform role. Source groups/roles/security keys are document grants, not platform roles.
# External subject/username link an existing identity-provider account; email alone does not link it.
workspace=$(_required SMARTSEARCH_WORKSPACE_ID)
source=$(_required SMARTSEARCH_SOURCE_ID)
group="${SMARTSEARCH_GRANT_GROUP:-everyone}"
body=$(jq -n --arg tenant "$tenant" --arg integration "$integration" --arg workspace "$workspace" --arg source "$source" --arg group "$group" '
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
    {
      workspace_id: $workspace,
      source_grants: [
        {
          source_id: $source,
          expected_revision: 0,
          grants: {
            groups: [
              $group
            ],
            roles: [
            ],
            security_keys: [
            ]
          }
        }
      ]
    }
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
  kind: "ONBOARD_USERS",
  items: [
    user(1;
    "Ada")
  ]
}
')
# Same idempotency key is safe ONLY with the unchanged request; new users/grants need a new key.
rest_register ONBOARD_USERS sdk-example-onboard-users-v1 "$body"

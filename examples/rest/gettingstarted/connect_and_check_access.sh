# Step 1: connect_and_check_access
# HTTP: GET /search-admin/api/provisioning/v1/capabilities (no request body)
# Problem: You have a service key. Does Search Admin recognize it, and which registration jobs can it submit?
# Run: ./run.sh connect_and_check_access [query]
# Needs: Admin/auth settings and a key with provisioning permission.
# Expect: Admin provisioning capabilities; not proof of project/Workplace access.
# Try: after this optional check, run the feature your key is assigned to.

# GET capabilities has no request body.

rest_capabilities

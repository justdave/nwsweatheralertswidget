#!/bin/sh

HOOK_DIR=".git/hooks"
PRE_COMMIT_HOOK="$HOOK_DIR/pre-commit"

# Ensure the hooks directory exists
mkdir -p "$HOOK_DIR"

# Create the pre-commit hook script
cat > "$PRE_COMMIT_HOOK" <<EOF
#!/bin/sh

# Check if PRIVACY.md is staged for commit
if git diff --cached --name-only | grep -q "^PRIVACY.md$"; then
    echo "Syncing PRIVACY.md to app assets..."
    cp PRIVACY.md app/src/main/assets/PRIVACY.md
    git add app/src/main/assets/PRIVACY.md
fi

# Check if CHANGES.md is staged for commit
if git diff --cached --name-only | grep -q "^CHANGES.md$"; then
    echo "Syncing CHANGES.md to app assets..."
    cp CHANGES.md app/src/main/assets/CHANGES.md
    git add app/src/main/assets/CHANGES.md
fi

EOF

# Make the hook executable
chmod +x "$PRE_COMMIT_HOOK"

echo "Pre-commit hook installed successfully."

FOR CONFLUENCE
----------------------------

One step you need to do manually:

Set the API token as an environment variable so Claude Code can read it from .mcp.json at startup:


# Windows (System Environment Variables or your shell profile)
setx CONFLUENCE_API_TOKEN "ATATT3xFfGF0nzq..."
Then restart Claude Code — it will:

Auto-load CLAUDE.md and know about the spec and Confluence page
Spawn the Confluence MCP server from .mcp.json
Allow you to run prompts like "fetch the latest spec from Confluence" or "update the spec page with these changes"


2. configure MCP server so that i can use it from claude code

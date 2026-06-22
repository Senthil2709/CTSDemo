# AI Banking Assistant — Claude Code Configuration

## Project Specification

The canonical project spec is stored in Confluence and mirrored locally.

- **Local copy**: C:\Demo\spec.md
- **Confluence page**: https://capstonedemo.atlassian.net/wiki/spaces/SD/pages/458753
- **Space key**: SD
- **Page title**: AI Banking Assistant — Project Spec

When answering questions about project requirements, tasks, or scope,
read `C:\Demo\spec.md` first. Use the Confluence MCP tool to fetch the
live page when the user asks for the latest published version.

## Project Structure

- Backend: C:\Demo\banking-assistant\banking-assistant\backend
- Package root: com.bankingassistant
- MCP module: com.bankingassistant.mcp (server + host)

## MCP Servers

- **confluence**: Publishes and reads documentation from capstonedemo.atlassian.net
  - Space: SD
  - Credentials: set via environment variables (see .mcp.json)

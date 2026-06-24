# QA Healthcare Context Builder Installation

**Toolset ID:** `qahc-context-builder`

## Overview
This document provides instructions for installing and setting up the QA Healthcare Context Builder toolset. This toolset helps AI agents build project-specific context for the QA Healthcare application — scanning codebase structure, generating structured documentation in L1/L2 format, and maintaining context accuracy through incremental updates and validation.

## AI Guided Installation
To install this toolset using AI Guided Installation, copy and paste the following prompt into your AI coding tool's chat window:

```shell
Follow instructions in .asdm/toolsets/qahc-context-builder/INSTALL.md
```

## Installation Steps

### 1. Create `.asdm/toolsets` and `.asdm/contexts` directories

Create directories for storing toolsets and generated context files:

```bash
mkdir -p .asdm/toolsets
mkdir -p .asdm/contexts/layer-2
mkdir -p .asdm/workspace/qahc-context/validation-reports
```

### 2. Detect the current `Agentic Engine` provider

Detect the current AI coding assistant provider (e.g., Claude Code, GitHub Copilot, Tencent CodeBuddy, Trae, Qoder, OpenCode). Using the following guidelines to detect the provider:

- If `.claude` directory exists, use `Claude Code`
- If `.github` directory exists, use `GitHub Copilot`
- If `.codebuddy` directory exists, use `Tencent CodeBuddy`
- If `.trae` directory exists, use `Trae`
- If `.qoder` directory exists, use `Qoder`
- If `.opencode` directory exists, use `OpenCode`
- If no such folder is found in the current workspace, give user a prompt to select a provider manually

### 3. Create shortcut commands for QAHC Context Builder (toolset ID: `qahc-context-builder`) in provider's entry point

> **Design Principle**: Command files should be **thin references** (single-line `follow` directives) that point to the actual action files under `.asdm/toolsets/qahc-context-builder/actions/`. This avoids duplicating content and ensures changes to action files are always reflected without re-installation.

#### For Tencent CodeBuddy (`.codebuddy/commands/`):

CodeBuddy commands use a simplified single-line format — just a `follow` directive pointing to the action file:

```bash
mkdir -p .codebuddy/commands/

# Initialize context command
cat > .codebuddy/commands/qahc-context-init.md << 'EOF'
follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-init.md
EOF

# Update context command
cat > .codebuddy/commands/qahc-context-update.md << 'EOF'
follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-update.md
EOF

# Validate context command
cat > .codebuddy/commands/qahc-context-validate.md << 'EOF'
follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-validate.md
EOF
```

#### For Claude Code (`.claude/commands/`):

Claude Code requires Markdown frontmatter metadata. Create commands by writing frontmatter followed by a `follow` directive as the body:

```bash
mkdir -p .claude/commands/

# Initialize context command
cat > .claude/commands/qahc-context-init.md << 'EOF'
---
description: "Initialize QA Healthcare project context (L1/L2 structure)"
---

follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-init.md
EOF

# Update context command
cat > .claude/commands/qahc-context-update.md << 'EOF'
---
description: "Incrementally update project context after code changes"
argument-hint: "[scope | full]"
---

follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-update.md
EOF

# Validate context command
cat > .claude/commands/qahc-context-validate.md << 'EOF'
---
description: "Validate existing context accuracy against actual codebase"
---

follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-validate.md
EOF
```

#### For GitHub Copilot (`.github/prompts/`):

GitHub Copilot uses `.prompt.md` files with YAML frontmatter. Create prompt files with frontmatter + `follow` directive body:

```bash
mkdir -p .github/prompts/

# Initialize context prompt
cat > .github/prompts/qahc-context-init.prompt.md << 'EOF'
---
agent: 'agent'
description: 'Initialize QA Healthcare project context (L1/L2 structure)'
---

follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-init.md
EOF

# Update context prompt
cat > .github/prompts/qahc-context-update.prompt.md << 'EOF'
---
agent: 'agent'
description: 'Incrementally update project context after code changes'
argument-hint: '[scope | full]'
---

follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-update.md
EOF

# Validate context prompt
cat > .github/prompts/qahc-context-validate.prompt.md << 'EOF'
---
agent: 'agent'
description: 'Validate existing context accuracy against actual codebase'
---

follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-validate.md
EOF
```

#### For Trae (`.trae/commands/`):

Trae uses Markdown command files with YAML frontmatter:

```bash
mkdir -p .trae/commands/

# Initialize context command
cat > .trae/commands/qahc-context-init.md << 'EOF'
---
name: qahc-context-init
description: "Initialize QA Healthcare project context (L1/L2 structure)"
---

follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-init.md
EOF

# Update context command
cat > .trae/commands/qahc-context-update.md << 'EOF'
---
name: qahc-context-update
description: "Incrementally update project context after code changes"
---

follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-update.md
EOF

# Validate context command
cat > .trae/commands/qahc-context-validate.md << 'EOF'
---
name: qahc-context-validate
description: "Validate existing context accuracy against actual codebase"
---

follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-validate.md
EOF
```

#### For Qoder (`.qoder/commands/`):

Qoder uses plain text command files — the filename is the command name, content is the prompt body, and descriptions are set separately in the Qoder UI:

```bash
mkdir -p .qoder/commands/

# Initialize context command
cat > .qoder/commands/qahc-context-init.md << 'EOF'
follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-init.md
EOF

# Update context command
cat > .qoder/commands/qahc-context-update.md << 'EOF'
follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-update.md
EOF

# Validate context command
cat > .qoder/commands/qahc-context-validate.md << 'EOF'
follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-validate.md
EOF
```

> **Note**: Qoder command descriptions must be set separately in the Qoder UI. It's recommended to add corresponding descriptions in the UI for better search and identification.

#### For OpenCode (`.opencode/commands/`):

OpenCode uses Markdown command files with YAML frontmatter, and supports `$ARGUMENTS` placeholders for passing arguments:

```bash
mkdir -p .opencode/commands/

# Initialize context command
cat > .opencode/commands/qahc-context-init.md << 'EOF'
---
description: "Initialize QA Healthcare project context (L1/L2 structure)"
---

follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-init.md
EOF

# Update context command
cat > .opencode/commands/qahc-context-update.md << 'EOF'
---
description: "Incrementally update project context after code changes"
---

follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-update.md
EOF

# Validate context command
cat > .opencode/commands/qahc-context-validate.md << 'EOF'
---
description: "Validate existing context accuracy against actual codebase"
---

follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-validate.md
EOF
```

> **Note**: OpenCode also supports defining commands directly in `opencode.json` via the `command` key with equivalent format.

### 4. Manual Usage for Other Providers

If your AI coding assistant provider is not detected by the automatic detection logic (Claude Code, GitHub Copilot, Tencent CodeBuddy, Trae, Qoder, or OpenCode), you can still use the QAHC Context Builder manually. Follow these steps:

#### Direct Instruction Usage
You can directly use the instruction files by copying their relative paths and pasting them into your AI coding assistant's chat window:

1. **Navigate to the instruction files**:
   ```bash
   cd .asdm/toolsets/qahc-context-builder/actions/
   ```

2. **Right-click on the desired instruction file** and copy its relative path:
   - For initializing context: `qahc-context-init.md`
   - For updating context: `qahc-context-update.md`
   - For validating context: `qahc-context-validate.md`

3. **Enter a prompt** in your AI coding assistant:
   ```
   follow {relative path to instruction file}
   ```
   Example: `follow .asdm/toolsets/qahc-context-builder/actions/qahc-context-init.md`

## Using QAHC Context Builder

### Step 1: Initialize Project Context (First Time / Rebuild)

Generate complete L1/L2 context structure by scanning the entire project:

```shell
/qahc-context-init
```

This will:
1. Scan `server/` (multi-service monorepo), `web/`, `deploy/` directories
2. Extract service list (`qa-service-{domain}/`), tech stack, coding style, data models, API endpoints, architecture decisions, deployment configs
3. Generate L1 entry file: `.asdm/contexts/index.md`
4. Generate L2 detail files under `.asdm/contexts/layer-2/` (6 spec documents)
5. Store scan cache at `.asdm/workspace/qahc-context/scan-cache.json`

**Use this when**: First time setup, or when you need to rebuild context from scratch

### Step 2: Update Context Incrementally

After code changes, update affected context files to stay in sync:

```shell
/qahc-context-update [scope]
```

This will:
1. Detect changes (via git diff, specified scope, or full scan mode)
2. Identify which context sections are affected
3. Update only the relevant L2 files
4. Regenerate L1 index if needed
5. Produce a change summary report

**Use this when**: After an iteration release, major refactoring, or periodic maintenance

### Step 3: Validate Context Accuracy

Check if existing context files match the actual codebase:

```shell
/qahc-context-validate
```

This will:
1. Compare each context section against actual source code
2. Flag stale items, inconsistencies, and missing information
3. Generate a validation report with diagnostic findings
4. Save report to `.asdm/workspace/qahc-context/validation-reports/validation-{date}.md`

**Use this when**: AI assistance behaves unexpectedly, or during regular quality audits

### Available Commands
Once installed, you can use the following commands:

| Command | Description |
|---------|-------------|
| `/qahc-context-init` | Initialize project context (first time or rebuild) |
| `/qahc-context-update [scope]` | Incrementally update context (scope optional, supports full mode) |
| `/qahc-context-validate` | Validate existing context accuracy |

### Recommended Workflow

```shell
# First-time setup or rebuild
/qahc-context-init

# After each iteration release
/qahc-context-update

# When AI assistance seems off
/qahc-context-validate

# Weekly quality audit
/qahc-context-validate
```

## Context Output Structure

QAHC Context Builder generates context files in a two-layer (L1/L2) structure:

```
.asdm/
├── contexts/                              # Project context knowledge base
│   ├── index.md                           # ★ L1 Entry file (must-read)
│   │                                       #    — Global overview, navigation index, dev guide
│   └── layer-2/                           # L2 Detailed context
│       ├── standard-project-structure.md  # Standard project structure & organization
│       ├── standard-coding-style.md       # Coding standards & style guide
│       ├── data-models.md                 # Data models, relationships & data flow
│       ├── api.md                         # API interface definitions & docs
│       ├── architecture.md                # System architecture & technical decisions
│       └── deployment.md                  # Deployment configuration & procedures
└── workspace/
    └── qahc-context/                      # Toolset runtime workspace
        ├── scan-cache.json               # Scan result cache
        └── validation-reports/           # Historical validation reports
            └── validation-{date}.md
```

## Toolset Structure

The QAHC Context Builder toolset has the following structure:

```
.asdm/toolsets/qahc-context-builder/
├── README.md                              ## Toolset description
├── INSTALL.md                             ## Installation instructions (this file)
├── manifest.json                          ## Toolset manifest
├── actions/                               ## Action instruction files
│   ├── qahc-context-init.md               ## Initialize project context (L1/L2)
│   ├── qahc-context-update.md             ## Incrementally update context
│   └── qahc-context-validate.md           ## Validate context accuracy
├── specs/                                 ## Spec templates (reference only)
│   ├── layer-1/
│   │   └── index.md                       ## L1 entry template
│   └── layer-2/
│       ├── standard-project-structure.md  ## Project structure template
│       ├── standard-coding-style.md       ## Coding style template
│       ├── data-models.md                 ## Data model template
│       ├── api.md                         ## API doc template
│       ├── architecture.md                ## Architecture template
│       └── deployment.md                  ## Deployment config template
└── docs/                                  ## Extended docs (reserved)
```

## Verification

After installation, verify that:

1. The `.asdm/contexts/` directory exists (with `layer-2/` subdirectory)
2. The `.asdm/workspace/qahc-context/` runtime workspace exists
3. Shortcut commands for QAHC Context Builder are created in the appropriate provider directory:
   - **Claude Code**: `.claude/commands/qahc-context-init.md`, `qahc-context-update.md`, `qahc-context-validate.md`
   - **GitHub Copilot**: `.github/prompts/qahc-context-init.prompt.md`, `qahc-context-update.prompt.md`, `qahc-context-validate.prompt.md`
   - **Tencent CodeBuddy**: `.codebuddy/commands/qahc-context-init.md`, `qahc-context-update.md`, `qahc-context-validate.md`
   - **Trae**: `.trae/commands/qahc-context-init.md`, `qahc-context-update.md`, `qahc-context-validate.md`
   - **Qoder**: `.qoder/commands/qahc-context-init.md`, `qahc-context-update.md`, `qahc-context-validate.md`
   - **OpenCode**: `.opencode/commands/qahc-context-init.md`, `qahc-context-update.md`, `qahc-context-validate.md`
4. Each command file contains only a thin reference (`follow .asdm/toolsets/qahc-context-builder/actions/<action>.md`) rather than duplicated content
5. The QAHC Context Builder toolset files are located in `.asdm/toolsets/qahc-context-builder` (toolset ID: `qahc-context-builder`)

**For other providers**: Verify that you can access the instruction files at:
- `.asdm/toolsets/qahc-context-builder/actions/qahc-context-init.md`
- `.asdm/toolsets/qahc-context-builder/actions/qahc-context-update.md`
- `.asdm/toolsets/qahc-context-builder/actions/qahc-context-validate.md`

## Notes

- This installation process assumes you have the necessary permissions to create directories and files
- Command files use **thin references** (`follow` directive) rather than copying action content, so updates to action files take effect immediately without re-installation
- The generated context files (`.asdm/contexts/`) are derived from **spec templates** in `specs/` — AI scans actual code and fills placeholders before output
- Context validation should be run periodically to ensure accuracy as the codebase evolves
- The `specs/` directory contains reference templates marked with template declarations; they are **not** the final context output
- All generated context follows ASDM design principles and the L1/L2 layered architecture pattern
- You can re-run `/qahc-context-init` anytime to rebuild context from scratch (overwrites existing)

## Integration with ASDM

QAHC Context Builder follows ASDM design principles and integrates with the existing ASDM ecosystem:
- Follows the standard toolset directory structure
- Uses the same action and spec conventions
- Supports all major AI coding assistants (Claude Code, GitHub Copilot, Tencent CodeBuddy, Trae, Qoder, OpenCode)
- Generates installation instructions compatible with ASDM
- Outputs context to the standard `.asdm/contexts/` location

### Getting Help
For issues with QAHC Context Builder, refer to:
- [ASDM Documentation](https://asdm.ai/docs)
- Toolset README: `.asdm/toolsets/qahc-context-builder/README.md`
- ASDM Design Principles: `.asdm/toolsets/toolset-builder/specs/ASDM_TOOLSET_DESIGN_PRINCIPLES.md`
- Toolset Development Training: `.asdm/toolsets/toolset-builder/specs/TOOLSET_DEV_TRAINING.md`

## License
Copyright (c) 2026 LeansoftX.com & iSoftStone. All rights reserved.

Licensed under the PROPRIETARY SOFTWARE LICENSE. See [LICENSE](LICENSE) in the project root for license information.

---

*This installation document is part of the QAHC Context Builder toolset. Use this toolset to maintain accurate AI-friendly context for the QA Healthcare project.*

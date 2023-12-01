---
name: Bug report
about: Create a report to help us improve
title: ''
labels: 'unlabeled'
assignees: adamico
body:
- type: dropdown
  id: os
  attributes:
    label: Which OS are you using?
    options:
      - MACOS
      - WINDOWS
      - LINUX
  validations:
    required: true
- type: input
  id: launcher
  attributes:
    label: Which Launcher?
    placeholder: "CurseForge Launcher, Prism Launcher, etc." 
  validations:
    required: true
- type: textarea
  id: bug_description
  attributes:
    label: Describe the bug
    description: "A clear and concise description of what the bug is."
  validations:
    required: true
- type: textarea
  id: repro
  attributes:
    label: To reproduce
    description: "How do you trigger this bug? Please walk us through it step by step."
    value: |
      Steps to reproduce the behavior:
      1. Start Launcher
      2. Install or Start Mechanical Mastery Plus <insert version number>
      3. Create / Load World / join server
      4. <action that causes the bug>
      5. error 
      ...
    render: bash
  validations:
    required: true
- type: textarea
    id: expected
    attributes:
      label: Expected behavior
      description: "A clear and concise description of what you expected to happen."
    validations:
      required: true
- type: textarea
  id: logs
  attributes:
    label: Logs
    description: "Paste here links to your `latest.log` and `crash-reports/crash-YYYY-MM-DD-HH.MM.SS-client.txt` files uploaded on `https://mclo.gs/` or `https://crashy.net` (if available)"
    value: |
      **latest.log**:
      **crashlog**: 
  validations:
    required: true
---
tete

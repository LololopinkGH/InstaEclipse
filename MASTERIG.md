# MasterIG development branch

This branch layers a separate MasterIG control surface and independently implemented hooks on top of the Apache-2.0 InstaEclipse project. It does not contain proprietary InstaPrime/Honista code or license bypasses.

Current additions:

- Long-press Instagram's bottom Home tab to open the searchable MasterIG control center.
- Existing InstaEclipse privacy/download/feed/appearance features exposed in one settings surface.
- Planned features are visibly disabled until a real hook exists; there are no placebo toggles.
- Mark Seen After Reply (experimental, requires device validation on Instagram 446/447).
- Always Expand Profile Bio (best effort).
- Optional hiding of InstaEclipse's manual Mark Seen eye button.
- Persistent MasterIG-specific preferences, isolated from upstream flags.
- A persistence layer reserved for the upcoming message edit-history hook.

Build output is produced by `.github/workflows/build-masterig.yml` as `masterig-module-debug`.

# Git Collaboration Guide For Group 91

## 1. Purpose
This guide defines a simple Git workflow for a six-person beginner team. The goal is to keep collaboration clear, reduce merge conflicts, and make each member's contribution visible on GitHub.

## 2. Branch Strategy
- The repository uses `main` as the only stable branch.
- Do not commit directly to `main` unless the team explicitly agrees on an emergency fix.
- Each member should create short-lived personal feature branches from `main`.
- Recommended branch naming format:
  - `name/feature`
  - Examples:
    - `long/profile-page`
    - `jojo/job-posting`
    - `alice/application-review`

## 3. Team Roles
- All six members should contribute to requirements, coding, testing, and documentation.
- Assign one member as `Scrum Master / coordinator`.
- Assign one member as `Backlog / document owner`.
- These coordination roles do not replace coding work. Everyone should still have visible branches and commits.

## 4. Suggested Task Split
- Member A: TA profile and CV upload
- Member B: TA job browsing and application submission
- Member C: MO job posting management
- Member D: MO application review and status update
- Member E: Admin workload dashboard and overload rules
- Member F: File storage, integration support, testing, README, and shared fixes

## 5. Daily Workflow
### 5.1 Start A Task
Always sync `main` before starting new work:

```bash
git checkout main
git pull origin main
```

Create a new branch for one task only:

```bash
git checkout -b long/profile-page
```

### 5.2 Work And Commit
- Keep each branch focused on one feature or one fix.
- Commit frequently with short, clear messages.
- Example commit messages:
  - `Add TA profile form`
  - `Implement job application submission`
  - `Fix workload calculation bug`
  - `Update SRS and acceptance criteria`

Standard commands:

```bash
git add .
git commit -m "Add TA profile form"
```

### 5.3 Push And Open A Pull Request
Push the branch to GitHub:

```bash
git push -u origin long/profile-page
```

Open a Pull Request from your branch into `main`.

### 5.4 Review And Merge
- At least one teammate should review the Pull Request before merging.
- After the Pull Request is merged, everyone should update local `main`:

```bash
git checkout main
git pull origin main
```

## 6. Team Rules
- Do not push directly to `main` for normal development.
- Do not use `git push --force`.
- Do not delete another member's branch without agreement.
- Do not mix multiple unrelated features in one branch.
- Pull the latest `main` before starting a new branch.
- If two people may edit the same file, discuss it first.

## 7. Pull Request Rules
Each Pull Request should be small enough to review quickly and should include:
- a clear title
- a short summary of what changed
- screenshots if the UI changed
- a short note on how it was tested

Recommended Pull Request title examples:
- `Add TA profile management page`
- `Implement MO job closing flow`
- `Add admin workload summary`

## 8. Conflict Reduction Rules
- Define shared JSON structure before parallel development starts.
- Define page fields and status names before UI and logic work are split.
- Avoid letting many people edit the same core file at the same time.
- Build the basic project structure first, then split features by module.

## 9. Sprint Practice
- At the start of each sprint:
  - confirm priority stories from the backlog
  - assign owners to each story
  - create branches only for planned work
- During the sprint:
  - hold short progress sync meetings
  - update GitHub issues or notes if work changes
- At the end of the sprint:
  - merge completed branches into `main`
  - review demo progress
  - record retrospective notes

## 10. What To Keep As Agile Evidence
The team should preserve these records for coursework:
- GitHub branches
- commits
- pull requests
- merge history
- meeting notes
- backlog updates
- prototype revisions

## 11. Minimum Setup Checklist
- Create the GitHub repository and confirm all members have access.
- Confirm the team uses `main` as the stable branch.
- Confirm branch naming format.
- Confirm commit message style.
- Confirm who is responsible for each first-sprint feature.
- Confirm who keeps meeting notes and backlog updates.

## 12. Recommended First Step
Before coding starts, one member should create the initial project skeleton, basic folder structure, and shared data model placeholders. After that, the team can split into separate feature branches with lower conflict risk.

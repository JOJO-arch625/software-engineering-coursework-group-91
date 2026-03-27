# Sprint 2 Handoff

## Current Core MVP Status
- Java Servlet/JSP web application skeleton is in place.
- Local file storage is used instead of a database.
- Seed JSON data is included under `data/`.
- Demo placeholder CV files are included under `uploads/cv/`.

## Implemented Flows
- `TA`
  - dashboard
  - profile create/update
  - CV upload with file type validation
  - job browsing
  - job detail
  - application submission with max-3 rule
  - applications status page
- `MO`
  - dashboard
  - create/edit posting
  - close posting
  - review applicants
  - mark application as `Under Review`, `Accepted`, or `Rejected`
- `Admin`
  - workload dashboard
  - accepted count summary
  - overload risk highlighting

## Important Coursework Rules Already Enforced
- No database is used.
- Closed jobs block new applications.
- A TA can apply to at most `3` jobs.
- A TA can be accepted to at most `3` jobs.
- Rejected applications remain visible to the TA.

## Suggested Teammate TODOs
- Optional AI fit scoring panel
- Missing-skill recommendations before TA submission
- Workload balancing recommendation logic for Admin/MO
- Extra validation polish and UX refinement
- Additional tests and demo walkthrough scripts

## Local Build Command
```powershell
mvn -q "-Dmaven.repo.local=.m2/repository" -DskipTests package
```

## Additional Documentation
- local run instructions: `docs/implementation/LOCAL-RUNNING-GUIDE.md`
- implemented features and TODO scope: `docs/implementation/FEATURE-STATUS.md`

## Main Entry Points
- `/gateway`
- `/ta/dashboard`
- `/ta/profile`
- `/ta/jobs`
- `/ta/job`
- `/ta/applications`
- `/mo/dashboard`
- `/mo/jobs/edit`
- `/mo/review`
- `/admin/workload`
- `/ai/assist`

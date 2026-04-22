# Implemented Features And TODO Scope

## Current Implementation Scope
This branch now delivers the `core intermediate-assessment MVP + role-based workspace version`.  
It is designed to be demo-ready, coursework-compliant, and still easy for teammates to extend.

## Implemented Features

### 1. Login And Role Access
- local login page
- local account seed data in `data/accounts.json`
- `HttpSession`-based login state
- role-based routing after login
- redirect to `/login` for unauthenticated access
- role-based route restriction
- logout

### 2. TA Flow
- TA dashboard with summary cards
- TA profile create and update
- local CV upload
- CV file type validation
- job list showing open jobs
- job detail page
- application submission
- application priority selection
- application status page
- visible rejection status for transparency

### 3. MO Flow
- MO dashboard
- create job posting
- edit job posting
- save job posting to local JSON storage
- close job posting
- applicant review page
- mark applicant as `Under Review`
- mark applicant as `Accepted`
- mark applicant as `Rejected`

### 4. Admin Flow
- workload dashboard
- accepted job count summary
- per-TA accepted module view
- overload risk highlighting at the threshold
- Admin positioned as a monitoring and balancing role rather than a direct hiring role

### 5. Current UI And Interaction
- unified blue and pink visual direction
- left-side role navigation
- top bar with user info
- page-level floating scroll button
- AI panels collapsed by default
- `Search` and `Inbox` preserved as TODO entry points instead of MVP features

## Core Rules Already Implemented
- no database is used
- all structured data is stored in JSON files
- CV files are stored locally
- a TA can apply to at most `3` jobs
- a TA can be accepted to at most `3` jobs
- a closed job blocks new applications
- rejected applications remain visible to the TA
- workload summary is derived from accepted applications
- AI, Search, and Inbox are outside the current MVP acceptance scope

## Data Model In Use
- `UserAccount`
- `TAProfile`
- `JobPosting`
- `ApplicationRecord`
- `WorkloadSummary`
- `OperationResult`
- `FlashMessage`

## Current Pages
### Entry routes
- `/`
- `/login`

### Main routes
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

## Explicit TODO Scope For Teammates

### AI And Enhancement Work
These areas are intentionally left as extension space and do not block the base demo:
- AI job fit scoring
- missing-skill suggestions before TA submission
- AI shortlist support for MO
- workload balancing advice for Admin

### Search And Inbox
These are already visible in the UI structure but not yet fully implemented:
- Search module
- Inbox / notification module
- cross-page search
- review reminders and system alerts

### Quality And UX Work
These are good follow-up tasks for teammates:
- stronger top-bar layout stability on different screen sizes
- more polished collapsed sidebar behavior
- AI page layout refinement
- stronger form validation polish
- better error text and edge-case handling
- UI refinement and accessibility improvements
- more stable demo walkthrough content
- README and user manual expansion

### Testing Work
These are still worth adding:
- page-by-page manual test checklist
- service-layer unit tests
- submission validation test cases
- workload calculation test cases
- login and role access test cases

## What Teammates Should Avoid Breaking
- the max-3 application rule
- the max-3 accepted rule
- closed-job blocking behavior
- local JSON storage design
- route names already used in the current JSP/Servlet structure
- login redirect and role-based access control

## Safe Extension Strategy
If teammates continue from this branch, the safest order is:
1. keep the current core flow stable
2. add tests
3. add UI polish
4. complete Search / Inbox
5. add optional AI support

## Suggested Ownership Split
- one teammate: Search / Inbox
- one teammate: AI fit scoring prototype
- one teammate: validation and UX polish
- one teammate: tests and demo scripts
- one teammate: README and user manual improvement

This keeps the current core MVP stable while still leaving meaningful work for the rest of the group.

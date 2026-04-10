# Frontend Test Document

## Scope

This folder contains the first round of frontend-focused automated tests for the TA Recruitment System.  
The project UI is server-rendered with JSP, so these tests validate the rendered HTML, redirects, flash messages, role-based navigation, and page-level user flows through HTTP responses.

## Current test target

- Login page rendering
- Route protection and role-based redirects
- TA dashboard, job list, job detail, and applications pages
- MO dashboard and applicant review page
- Admin workload dashboard

## Test strategy

The current suite uses a lightweight in-repo Node runner instead of browser automation or `node --test`.  
This choice keeps the test code lightweight and also avoids the sandbox child-process issue that can appear in restricted environments.

Main ideas:

- Treat JSP output as the frontend contract
- Assert visible user-facing text instead of fragile CSS selectors
- Keep most checks read-only so the seeded demo data is not mutated
- Cover the most important acceptance paths before adding deeper UI automation

## Files

- `frontend/login-and-routing.test.mjs`: login, session, redirect, and permission checks
- `frontend/ta-pages.test.mjs`: TA-facing page coverage
- `frontend/mo-admin-pages.test.mjs`: MO and Admin page coverage
- `run-frontend-tests.mjs`: lightweight entry point for executing the suite
- `helpers/miniTestRunner.mjs`: internal test runner used by this folder
- `helpers/sessionClient.mjs`: session-aware HTTP helper with cookie handling
- `helpers/htmlAssertions.mjs`: shared text and title assertions
- `helpers/testConfig.mjs`: base URL and demo account configuration

## Covered scenarios

### 1. Login and route protection

- Login page renders correctly
- Demo account hints are visible
- Unauthenticated users are redirected to `/login`
- TA login redirects to `/ta/dashboard`
- Invalid login shows an error message
- TA users are blocked from visiting Admin-only pages

### 2. TA frontend flow

- TA job list only shows open postings
- Closed postings are hidden from the TA list
- Application history keeps rejected records visible
- Job detail page exposes the application form
- Optional AI section is still visible as a non-core enhancement block

### 3. MO and Admin flow

- MO dashboard shows posting management context
- MO review page displays applicant details and decision controls
- Admin workload dashboard shows threshold and overload status

## Why this counts as frontend testing

Although these tests do not open a browser yet, they still validate frontend behavior because the UI here is rendered on the server side.  
The important frontend contract for this project is:

- which page a user reaches
- what text and actions they see
- whether the correct role-specific page is rendered
- whether important workflow messages are visible

## Limitations in this first version

- No visual regression coverage
- No CSS/layout assertions
- No JavaScript interaction coverage beyond server-rendered flows
- No mutation-heavy scenario checks such as repeated application submission or MO decision updates
- Tests require the web app to be started manually before execution

## Recommended next steps

1. Keep this suite as a stable smoke/acceptance layer.
2. Add backend/service tests for business rules such as application caps and workload calculation.
3. Add browser-level automation later with Playwright or Selenium if the team wants UI interaction coverage.
4. Add test fixtures or data reset scripts before introducing write-heavy end-to-end tests.

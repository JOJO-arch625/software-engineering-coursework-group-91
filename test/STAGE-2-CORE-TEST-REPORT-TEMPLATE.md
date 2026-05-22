# Stage 2 Core Test Report

## Scope

- S2-01 TA job list deadline sorting and MO name display
- S2-02 Shortlisted status, single MO action, bulk MO action, and TA sync
- S2-03 TA motivation notes separated from MO reviewer notes
- S2-04 Admin weekly-hour workload totals, overload highlight, and TA search
- S2-05 Explainable AI job recommendations with visible match calculation
- S2-06 Backend contract tests, Java model tests, and frontend page checks

## Test Execution

| Suite | Command | Expected Result | Actual Result | Pass Rate | Notes |
| --- | --- | --- | --- | --- | --- |
| Backend Java model tests | `powershell -ExecutionPolicy Bypass -File test/backend/java/run-java-tests.ps1` | All model field and helper tests pass |  |  |  |
| Backend contract tests | `node test/run-backend-contract-tests.mjs` | Service, storage, and servlet contracts pass |  |  |  |
| Frontend integration checks | `node test/run-frontend-tests.mjs` | TA, MO, Admin rendered page checks pass |  |  | Requires local web app server |

## Core Acceptance Cases

| ID | Requirement | Steps | Expected Result | Actual Result | Pass/Fail |
| --- | --- | --- | --- | --- | --- |
| S2-E2E-01 | Deadline sorting and MO display | Log in as TA and open Browse Job Postings | Open jobs are sorted by nearest deadline and each job shows Responsible MO |  |  |
| S2-E2E-02 | Single Shortlisted action | Log in as MO, open Applicant Review, mark one application Shortlisted | MO review list and TA applications page both show orange Shortlisted status |  |  |
| S2-E2E-03 | Bulk Shortlisted action | Log in as MO and use Bulk shortlist current job | Eligible non-accepted and non-rejected applicants for the job become Shortlisted |  |  |
| S2-E2E-04 | Reviewer notes do not overwrite motivation | TA submits motivation note, MO adds reviewer note | TA motivation and MO reviewer note are displayed separately and neither is lost |  |  |
| S2-E2E-05 | Weekly-hour workload | Open Admin Workload Dashboard | Weekly hours are summed per TA and totals above 10 are highlighted |  |  |
| S2-E2E-06 | Admin TA search | Search by TA ID or TA name on Admin Workload Dashboard | Rows filter to matching TA records |  |  |
| S2-E2E-07 | Explainable recommendation | Log in as TA and open dashboard | Top recommendations above 60% show matched skill count and match rate formula text |  |  |

## Defect Log

| ID | Found In | Description | Severity | Owner | Status |
| --- | --- | --- | --- | --- | --- |
|  |  |  |  |  |  |

## Summary

- Date:
- Tester:
- Environment:
- Suites executed:
- Total passed:
- Total failed:
- Release decision:

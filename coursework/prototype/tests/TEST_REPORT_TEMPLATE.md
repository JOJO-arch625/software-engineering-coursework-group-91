# Stage 2 Core Test Report

## Scope
- S2-01 deadline sorting and MO name display
- S2-02 Shortlisted status and batch/single MO action
- S2-03 reviewer notes separated from TA motivation notes
- S2-04 admin weekly-hour workload and TA search
- S2-05 explainable AI job recommendations

## Execution
- Unit tests: `node coursework/prototype/tests/core.test.js`
- Manual end-to-end checks: open `coursework/prototype/index.html` in a browser and follow the scenarios below.

## Manual End-To-End Cases
| ID | Scenario | Expected result | Pass/Fail | Notes |
| --- | --- | --- | --- | --- |
| E2E-01 | Open Job List | Jobs appear in deadline ascending order and each card shows the MO name |  |  |
| E2E-02 | MO marks one applicant Shortlisted | Review table and TA Applications page show orange Shortlisted state |  |  |
| E2E-03 | MO uses Bulk shortlist current job | Eligible applicants for the selected posting become Shortlisted |  |  |
| E2E-04 | TA submits motivation, MO writes reviewer notes | TA motivation remains visible and reviewer notes appear separately |  |  |
| E2E-05 | Open Admin Dashboard and search by TA ID/name | Rows filter correctly; weekly hours above 10 are highlighted |  |  |
| E2E-06 | Open TA Dashboard recommendations | Recommended jobs show match percentage and matched-skill explanation |  |  |

## Summary
- Date:
- Tester:
- Unit test pass rate:
- E2E pass rate:
- Bugs found:
- Fix status:

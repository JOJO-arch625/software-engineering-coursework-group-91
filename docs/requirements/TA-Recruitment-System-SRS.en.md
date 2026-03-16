# Teaching Assistant Recruitment System Software Requirements Specification

## 1. Purpose
This document defines the background, scope, functional requirements, non-functional requirements, data storage strategy, acceptance criteria, and agile development approach for the BUPT International School Teaching Assistant Recruitment System (TARS). It is the English SRS aligned with the Chinese requirements master draft and is intended to support backlog creation, prototyping, implementation planning, and coursework submission.

## 2. Project Background And Coursework Constraints
### 2.1 Background
BUPT International School recruits Teaching Assistants (TAs) each semester to support academic modules. The current process relies heavily on forms and Excel files for job posting, application collection, applicant review, and workload tracking. The proposed system will provide a unified and simple platform for TAs, Module Organisers (MOs), and administrators to complete the recruitment workflow more efficiently.

### 2.2 Coursework Constraints
- Agile methods must be applied throughout requirements, analysis, design, implementation, and testing.
- The system must be developed either as a stand-alone Java application or a lightweight Java Servlet/JSP web application. This project adopts a lightweight Java Servlet/JSP web application.
- Databases are not allowed. All input and output data must be stored in simple text-based files.
- The product must support staged assessment, including backlog, prototype, working versions, and final delivery.

### 2.3 Current Assumptions
- No direct access to real interview participants is available at this stage.
- The first version of the requirements is based on handout analysis, team workshops, and proxy assumptions.
- If real stakeholder feedback becomes available later, the backlog should be refined accordingly.

## 3. Stakeholders And User Roles
### 3.1 Stakeholders
- TA applicants: want to browse jobs, submit applications, and track results.
- MOs: want to publish jobs, review applicants, and make hiring decisions.
- Administrators: want to monitor overall TA workload and avoid overload.
- Teaching team: expects clear requirements, agile evidence, and verifiable engineering work.
- Project team members: need visible contribution evidence through GitHub branches, commits, and meeting records.

### 3.2 User Roles
- `TA`: applicant role that manages profile data, uploads CV, and applies for jobs.
- `MO`: organiser role that creates jobs and handles applications.
- `Admin`: management role that views workload across accepted TA assignments.

## 4. Scope And Exclusions
### 4.1 MVP Scope
The MVP covers module TA recruitment only and excludes invigilation and other school activities. The system supports the following core workflows:
- TA creates or updates profile information.
- TA uploads and replaces CV files.
- TA browses open job postings and submits applications.
- TA checks application status.
- MO creates, edits, and closes job postings.
- MO reviews applicants, checks profiles and CVs, and accepts or rejects applications.
- Admin views overall TA workload and identifies overload risks.

### 4.2 Exclusions
The following items are out of scope for the MVP:
- AI matching, recommendation, or explanation features.
- Email, SMS, or in-system notifications.
- External authentication or integration with existing university accounts.
- Databases, Spring Boot, or other advanced frameworks.
- Mobile applications.
- Complex multi-school or multi-term administration.
- Recruitment workflows beyond module TA hiring.

## 5. Business Workflow Overview
### 5.1 TA Workflow
1. TA enters the TA role interface.
2. TA creates or updates a personal profile.
3. TA uploads or replaces a CV.
4. TA browses the job list and opens job details.
5. TA submits an application for an open job.
6. TA checks application status on the personal applications page.

### 5.2 MO Workflow
1. MO enters the MO role interface.
2. MO creates a job posting with module information, duties, schedule, and requirements.
3. MO edits or closes a job posting.
4. MO views the applicant list for a job.
5. MO reviews candidate profile data and CV.
6. MO updates each application to an accepted or rejected decision.

### 5.3 Admin Workflow
1. Admin enters the administration interface.
2. Admin views all accepted TA assignments.
3. Admin identifies TAs whose accepted workload exceeds the configured threshold.

## 6. Functional Requirements
### 6.1 Account And Role Management
- `FR-001` The system shall support role-based entry to different functional interfaces.
- `FR-002` The system shall restrict access so that users can only perform actions allowed by their role.
- `FR-003` The system shall load account and role information from local files.

### 6.2 TA Functions
- `FR-101` A TA shall be able to create a personal profile including name, student number or unique ID, contact details, skills, and availability.
- `FR-102` A TA shall be able to update an existing profile.
- `FR-103` A TA shall be able to upload a CV file and replace it later.
- `FR-104` The system shall display all open jobs to TAs.
- `FR-105` A TA shall be able to view job details including module name, responsibilities, requirements, deadline, and status.
- `FR-106` A TA shall be able to submit an application for an open job.
- `FR-107` A TA shall not be able to apply for a closed job.
- `FR-108` A TA shall be able to view all personal applications and their statuses.

### 6.3 MO Functions
- `FR-201` An MO shall be able to create a job posting and save its details.
- `FR-202` An MO shall be able to edit an active job posting.
- `FR-203` An MO shall be able to close a job posting so that it no longer accepts applications.
- `FR-204` An MO shall be able to view all applications for a job posting.
- `FR-205` An MO shall be able to view applicant profile data and the CV path or download entry.
- `FR-206` An MO shall be able to update an application status to `Under Review`, `Accepted`, or `Rejected`.
- `FR-207` The system shall persist application status changes and reflect them in the TA interface.

### 6.4 Admin Functions
- `FR-301` An Admin shall be able to view TA assignment records for accepted jobs.
- `FR-302` The system shall summarise accepted job counts by TA.
- `FR-303` The system shall support workload overload marking based on a predefined threshold.

### 6.5 Data And File Handling
- `FR-401` The system shall store structured business data in JSON files.
- `FR-402` The system shall store uploaded CV files in a local folder and record the file path in JSON data.
- `FR-403` The system shall read existing data files at startup and write updates after user operations.
- `FR-404` The system shall provide understandable error messages when data files are missing or malformed.

## 7. Non-Functional Requirements
- `NFR-001` The interface shall remain clear and allow access to key actions within three clicks.
- `NFR-002` The system shall use a modular design so future AI or additional recruitment scenarios can be added later.
- `NFR-003` The system shall avoid advanced frameworks to remain aligned with coursework constraints and simple deployment.
- `NFR-004` The system shall provide basic validation for required fields, file type checks, and role-based access control.
- `NFR-005` The system shall reduce obvious data loss risk by reporting file write failures clearly.
- `NFR-006` The system shall run reliably during demonstrations and support smooth walkthroughs of major workflows.

## 8. Data Model And File Storage Strategy
### 8.1 Core Entities
- `UserAccount`: `userId`, `username`, `role`
- `TAProfile`: `taId`, `name`, `studentNumber`, `email`, `phone`, `skills`, `availability`, `cvPath`
- `Module`: `moduleId`, `moduleCode`, `moduleName`, `organiserId`
- `JobPosting`: `jobId`, `moduleId`, `title`, `description`, `requirements`, `deadline`, `status`
- `Application`: `applicationId`, `jobId`, `taId`, `submittedAt`, `status`
- `WorkloadSummary`: `taId`, `acceptedJobsCount`, `acceptedJobIds`, `overloadFlag`

### 8.2 Suggested File Layout
- `data/accounts.json`
- `data/ta_profiles.json`
- `data/modules.json`
- `data/job_postings.json`
- `data/applications.json`
- `data/config.json`
- `uploads/cv/`

### 8.3 Storage Rules
- All structured business data shall be stored in JSON by default.
- `config.json` may define workload thresholds, allowed CV file types, and maximum file size.
- CSV export may be added later for reporting, but JSON remains the primary storage format.

## 9. Acceptance Criteria
### 9.1 Core Acceptance Scenarios
- `AC-001` When a TA submits complete profile data and a valid CV, the system shall save the profile and show a success message.
- `AC-002` When a TA browses jobs, the system shall show only jobs with open status.
- `AC-003` When a TA applies for an open job, the system shall create an application record with status `Submitted`.
- `AC-004` When a TA attempts to apply for a closed job, the system shall block submission and show an unavailable message.
- `AC-005` When an MO creates and saves a job posting, the job shall appear in the open jobs list.
- `AC-006` When an MO closes a job, the system shall no longer allow new TA applications for that job.
- `AC-007` When an MO updates an application to `Accepted` or `Rejected`, the TA applications page shall show the updated status.
- `AC-008` When an Admin opens the workload page, the system shall display accepted job counts for each TA and flag overload cases.
- `AC-009` When a user submits data with missing required fields, the system shall reject the request and show a clear validation message.
- `AC-010` When a user uploads an invalid file type, the system shall reject the upload and explain the reason.

### 9.2 Acceptance Testing Principles
- Every high-priority user story must include directly testable acceptance criteria.
- Each role must have at least one success path and one failure or validation path.
- Acceptance criteria must support the final demo/viva acceptance testing tasks.

## 10. Agile Development Approach
### 10.1 Selected Method
This project adopts `Scrum-lite` as its agile delivery approach for the following reasons:
- The coursework explicitly requires Agile methods throughout the project.
- The project scope is limited enough that full Scrum would add unnecessary process overhead.
- Scrum-lite is sufficient to support backlog management, iteration planning, review, retrospective, and continuous improvement.

### 10.2 Team Workflow
- Create and maintain a `Product Backlog`.
- Hold `Sprint Planning` before each sprint to define the sprint goal and selected stories.
- Use GitHub branch-based collaboration and short team sync meetings during the sprint.
- Hold a `Sprint Review` at the end of each sprint to inspect completed work and issues.
- Hold a `Retrospective` after review and update the backlog for the next sprint.

### 10.3 Team Responsibilities
- All team members participate in requirements, design, implementation, testing, and documentation.
- One member is assigned as `Scrum Master / coordinator` to organise meetings and track progress.
- One member is assigned as `Product Backlog Owner` to maintain stories, while requirement decisions remain collaborative.

### 10.4 Agile Evidence
The following artefacts serve as agile evidence:
- GitHub branches, commits, pull requests, and merge history.
- Meeting notes from team discussions.
- Incremental backlog update records.
- Prototype revisions and working software version history.

## 11. Release And Iteration Plan
### 11.1 Sprint 1
- Analyse the coursework handout.
- Define user roles, scope, and MVP boundaries.
- Produce the first draft of user stories and the product backlog.
- Produce the prototype.
- Define the core data model and file storage strategy.
- Deliver first assessment materials.

### 11.2 Sprint 2
- Implement TA-side core workflows: profile management, CV upload, job browsing, application submission, and status checking.
- Deliver Working Software Version 1 and record discovered issues.

### 11.3 Sprint 3
- Implement MO-side job management and application handling.
- Implement Admin workload viewing.
- Fix major issues found in Version 1.
- Deliver Working Software Version 2.

### 11.4 Sprint 4
- Improve validation and error handling.
- Complete testing, user manual, README, and demonstration materials.
- Prepare final submission and demo/viva.

### 11.5 Iteration Adjustment Rule
- After each sprint, update the backlog based on review outcomes, feedback, and retrospective notes.
- Complete Must-have recruitment workflows before considering lower-priority enhancements.

## 12. Mapping To First Assessment Deliverables
### 12.1 Product Backlog
The backlog must use the provided coursework template fields exactly:
- `Story ID`
- `Story Name`
- `Description`
- `Priority`
- `Iteration (Sprint) number`
- `Acceptance Criteria`
- `Estimation`
- `Notes`
- `Date started (or planned)`
- `Date finished (or planned)`

User stories should be derived directly from the functional requirements and acceptance criteria in this document.

### 12.2 Prototype
The prototype should cover the full intended workflow of the target system, not only the subset to be implemented first. Each role should appear in the prototype with at least one success path and one failure or validation path.

### 12.3 Brief Report
The brief report should focus on:
- fact-finding techniques
- iteration plan
- prioritisation
- estimation methods

At the current stage, the evidence sources should be stated honestly as:
- handout analysis
- team workshop
- proxy assumptions

If real stakeholder input becomes available later, it can be added as supporting material.

## 13. Future Enhancements
- Skill-based matching suggestions for jobs and applicants.
- Suggestions for missing applicant skills.
- More detailed workload balancing recommendations.
- Email or in-system notifications.
- Support for additional recruitment activity types.

## 14. Assumptions And Open Points
- There is no existing project repository yet, so a GitHub repository should be created soon.
- The main branch is assumed to be `master`, and all team members should work through visible personal branches before merging.
- No real stakeholder interviews are currently available, so the evidence remains a first-pass analysis baseline.
- If the team later narrows or expands scope, this document, the backlog, and the prototype must be updated together.

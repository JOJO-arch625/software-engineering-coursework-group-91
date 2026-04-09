# Local Running Guide

## Overview
This project is a lightweight `Java Servlet/JSP` web application for the TA Recruitment System coursework.  
It uses:
- `Maven` for build and local run
- `Tomcat Maven Plugin` for local development
- `JSON` files for structured data
- local files for uploaded CVs

No database is required.

## Prerequisites
- `Java 8`
- `Maven`
- Internet access for the first plugin or dependency download

## Project Paths
- source code: `src/main/java`
- JSP pages: `src/main/webapp/WEB-INF/jsp`
- styles: `src/main/webapp/assets/styles/app.css`
- seed data: `data/`
- CV files: `uploads/cv/`

## Build The Project
Run:

```powershell
mvn -q "-Dmaven.repo.local=.m2/repository" -DskipTests package
```

Expected result:
- a successful build
- generated WAR file at `target/ta-recruitment-system.war`

## Run The Project Locally
Run:

```powershell
mvn "org.apache.tomcat.maven:tomcat7-maven-plugin:2.2:run" "-Dmaven.repo.local=.m2/repository"
```

When the server starts successfully, open:

- `http://127.0.0.1:8080/`
- or `http://127.0.0.1:8080/gateway`

## Main Local Routes
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

## Stop The Local Server
If the server is running in the current terminal:
- press `Ctrl + C`

If it is running in the background:

```powershell
Get-Process java
Stop-Process -Id <PID>
```

## Demo Data
The project includes stable seed data so teammates can see the same baseline:
- `data/ta-profiles.json`
- `data/job-postings.json`
- `data/applications.json`

Placeholder CV files are included in:
- `uploads/cv/`

## Common Problems
### 1. Maven cannot download plugins or dependencies
This usually happens on the first run if the network is blocked.  
Retry the command when network access is available.

### 2. Port 8080 is already in use
Close the conflicting process or stop the old Java/Tomcat instance before starting again.

### 3. Data looks inconsistent
Check whether someone manually edited:
- `data/ta-profiles.json`
- `data/job-postings.json`
- `data/applications.json`

### 4. Uploaded CV validation fails
Only these file types are accepted:
- `.pdf`
- `.doc`
- `.docx`

## Recommended Demo Order
1. Open `/gateway`
2. Show the TA flow
3. Show the MO flow
4. Show the Admin workload page
5. Show `/ai/assist` only as optional future enhancement

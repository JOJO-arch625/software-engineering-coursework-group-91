# Local Running Guide

## Overview
This project is a lightweight `Java Servlet/JSP` web application for the TA Recruitment System coursework.  
It currently uses:
- `Maven` for build and local run
- `Tomcat Maven Plugin` for local development
- `JSON` files for structured data
- local files for uploaded CVs
- `HttpSession` for lightweight login and role-based access

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
- account data: `data/accounts.json`
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
Recommended PowerShell command on this machine:

```powershell
C:\Users\Chencc\Desktop\gsxk\apache-maven-3.5.0\bin\mvn.cmd org.apache.tomcat.maven:tomcat7-maven-plugin:2.2:run "-Dmaven.repo.local=.m2\repository"
```

Notes:
- this uses the working Maven installation path already available on the machine
- the `-Dmaven.repo.local` argument should stay quoted in PowerShell

When the server starts successfully, open:

- `http://127.0.0.1:8080/login`
- or `http://localhost:8080/login`

## Entry Points And Main Routes
### Login entry
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

## Demo Accounts
### TA
- username: `ta.demo`
- password: `TaDemo123`

### MO
- username: `mo.demo`
- password: `MoDemo123`

### Admin
- username: `admin.demo`
- password: `AdminDemo123`

## Current Login Behavior
- all users enter through `/login`
- successful login redirects by role:
  - `TA -> /ta/dashboard`
  - `MO -> /mo/dashboard`
  - `Admin -> /admin/workload`
- unauthenticated users are redirected back to the login page
- authenticated users can only access the routes allowed for their role

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
- `data/accounts.json`
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
If needed, run on another port:

```powershell
C:\Users\Chencc\Desktop\gsxk\apache-maven-3.5.0\bin\mvn.cmd org.apache.tomcat.maven:tomcat7-maven-plugin:2.2:run "-Dmaven.repo.local=.m2\repository" "-Dmaven.tomcat.port=8081"
```

### 3. The page still looks like an old cached version
Use a hard refresh:
- `Ctrl + F5`

### 4. Data looks inconsistent
Check whether someone manually edited:
- `data/accounts.json`
- `data/ta-profiles.json`
- `data/job-postings.json`
- `data/applications.json`

### 5. Uploaded CV validation fails
Only these file types are accepted:
- `.pdf`
- `.doc`
- `.docx`

## Recommended Demo Order
1. Open `/login`
2. Show the TA flow with `ta.demo`
3. Show the MO flow with `mo.demo`
4. Show the Admin workload page with `admin.demo`
5. Show `/ai/assist` only as optional future enhancement

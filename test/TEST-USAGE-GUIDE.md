# Frontend Test Usage Guide

## What this folder gives you

This folder adds a lightweight frontend test suite for the current JSP/Servlet project.  
It is designed to run independently from Maven test plugins and only depends on Node.js, which is already available in the current environment.

## Folder structure

```text
test/
  package.json
  FRONTEND-TEST-DOCUMENT.md
  TEST-USAGE-GUIDE.md
  run-frontend-tests.mjs
  helpers/
    htmlAssertions.mjs
    miniTestRunner.mjs
    sessionClient.mjs
    testConfig.mjs
  frontend/
    login-and-routing.test.mjs
    ta-pages.test.mjs
    mo-admin-pages.test.mjs
```

## Before running the tests

You must start the TA Recruitment System first.  
The suite expects the application to be reachable at:

```text
http://127.0.0.1:8080
```

If your app runs on another host or port, set `FRONTEND_BASE_URL`.

## Default execution

From the project root:

```powershell
npm --prefix test run test:frontend
```

Verbose reporter:

```powershell
npm --prefix test run test:frontend:verbose
```

## Run against a custom address

PowerShell example:

```powershell
$env:FRONTEND_BASE_URL = "http://127.0.0.1:8081"
npm --prefix test run test:frontend
```

## What the tests actually do

- Send requests to the running web application
- Maintain login session cookies
- Verify redirects and permission boundaries
- Inspect returned HTML for important user-facing content

Internally the suite uses a small custom runner so it can still execute in environments where `node --test` child processes are restricted.

## Important notes

- The current suite is mostly read-only and avoids changing demo data.
- Login requests do create normal session state, but they do not change JSON business data.
- If the server is not running, the tests fail early with a clear message.

## If a test fails

Use this order:

1. Confirm the application is running.
2. Confirm the base URL matches the running port.
3. Confirm the demo accounts in `data/accounts.json` still match the credentials in `helpers/testConfig.mjs`.
4. Check whether page titles or visible text changed in the JSP files.

## How to extend the suite

Add a new `.test.mjs` file under `test/frontend/`.

Suggested pattern:

1. Create a new `SessionClient`.
2. Log in with the role you need.
3. Call `getHtml()` or `request()`.
4. Assert status code, redirect target, and visible text.

## Suggested future upgrades

- Add data reset helpers for write-heavy scenarios
- Add service-layer tests under a separate backend test folder
- Upgrade to browser automation when you want true click/form/UI interaction coverage

import { MiniTestRunner } from "./helpers/miniTestRunner.mjs";
import registerLoginAndRoutingTests from "./frontend/login-and-routing.test.mjs";
import registerTaPageTests from "./frontend/ta-pages.test.mjs";
import registerMoAndAdminPageTests from "./frontend/mo-admin-pages.test.mjs";

import { registerMoWorkflowTests } from "./frontend/mo-workflow.test.mjs";
import { registerAdminOverloadTests } from "./frontend/admin-overload.test.mjs";
import { registerSecurityNegativeTests } from "./frontend/security-negative-tests.mjs";

import { registerTaProfileTests } from "./frontend/ta-profile.test.mjs";
import { registerTaApplicationRulesTests } from "./frontend/ta-application-rules.test.mjs";
import { registerLogoutTests } from "./frontend/logout.test.mjs";

const runner = new MiniTestRunner();
const verbose = process.argv.includes("--verbose");

registerLoginAndRoutingTests(runner);
registerTaPageTests(runner);
registerMoAndAdminPageTests(runner);

registerMoWorkflowTests(runner);
registerAdminOverloadTests(runner);
registerSecurityNegativeTests(runner);

registerTaProfileTests(runner);
registerTaApplicationRulesTests(runner);
registerLogoutTests(runner);

const success = await runner.run({ verbose });
process.exitCode = success ? 0 : 1;
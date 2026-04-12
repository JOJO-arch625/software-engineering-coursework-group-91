import { MiniTestRunner } from "./helpers/miniTestRunner.mjs";
import registerLoginAndRoutingTests from "./frontend/login-and-routing.test.mjs";
import registerTaPageTests from "./frontend/ta-pages.test.mjs";
import registerMoAndAdminPageTests from "./frontend/mo-admin-pages.test.mjs";

const runner = new MiniTestRunner();
const verbose = process.argv.includes("--verbose");

registerLoginAndRoutingTests(runner);
registerTaPageTests(runner);
registerMoAndAdminPageTests(runner);

const success = await runner.run({ verbose });
process.exitCode = success ? 0 : 1;

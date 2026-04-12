import { MiniTestRunner } from "./helpers/miniTestRunner.mjs";
import registerServiceRuleTests from "./backend/contracts/service-rules.test.mjs";
import registerStorageSeedDataTests from "./backend/contracts/storage-seed-data.test.mjs";
import registerServletRoutingTests from "./backend/contracts/servlet-routing.test.mjs";

const runner = new MiniTestRunner();
const verbose = process.argv.includes("--verbose");

registerServiceRuleTests(runner);
registerStorageSeedDataTests(runner);
registerServletRoutingTests(runner);

const success = await runner.run({ verbose });
process.exitCode = success ? 0 : 1;

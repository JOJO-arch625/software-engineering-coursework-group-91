function formatError(error) {
  if (!error) {
    return "Unknown error";
  }

  if (error.stack) {
    return error.stack;
  }

  return String(error);
}

export class MiniTestRunner {
  constructor() {
    this.suites = [];
  }

  suite(name, defineSuite) {
    const suite = {
      name,
      beforeHooks: [],
      beforeEachHooks: [],
      tests: []
    };

    defineSuite({
      before: (hook) => suite.beforeHooks.push(hook),
      beforeEach: (hook) => suite.beforeEachHooks.push(hook),
      test: (testName, fn) => suite.tests.push({ name: testName, fn })
    });

    this.suites.push(suite);
  }

  async run(options = {}) {
    const verbose = Boolean(options.verbose);
    let passed = 0;
    let failed = 0;

    for (const suite of this.suites) {
      console.log(`\n[Suite] ${suite.name}`);

      try {
        for (const hook of suite.beforeHooks) {
          await hook();
        }
      } catch (error) {
        failed += suite.tests.length || 1;
        console.log("  FAIL before hook");
        console.log(`  ${formatError(error)}`);
        continue;
      }

      for (const item of suite.tests) {
        try {
          for (const hook of suite.beforeEachHooks) {
            await hook();
          }

          await item.fn();
          passed += 1;
          console.log(`  PASS ${item.name}`);
        } catch (error) {
          failed += 1;
          console.log(`  FAIL ${item.name}`);
          console.log(`  ${formatError(error)}`);
        }
      }

      if (verbose) {
        console.log(`  Completed ${suite.tests.length} test(s) in suite.`);
      }
    }

    console.log(`\nSummary: ${passed} passed, ${failed} failed.`);

    return failed === 0;
  }
}

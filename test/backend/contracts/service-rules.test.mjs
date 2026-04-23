import assert from "node:assert/strict";
import { readProjectFile } from "../../helpers/projectFiles.mjs";

export default function registerServiceRuleTests(runner) {
  runner.suite("后端契约测试：服务层核心规则", ({ before, test }) => {
    let source;

    before(async () => {
      source = await readProjectFile(
        "src",
        "main",
        "java",
        "com",
        "group91",
        "tars",
        "service",
        "TarsService.java"
      );
    });

    test("申请上限和录取上限常量保持为 3", () => {
      assert.match(source, /MAX_APPLICATIONS\s*=\s*3/);
      assert.match(source, /MAX_ACCEPTED_JOBS\s*=\s*3/);
    });

    test("角色主页跳转包含 TA、MO、ADMIN 三种分流", () => {
      assert.match(source, /ROLE_MO/);
      assert.match(source, /ROLE_ADMIN/);
      assert.match(source, /return "\/mo\/dashboard";/);
      assert.match(source, /return "\/admin\/workload";/);
      assert.match(source, /return "\/ta\/dashboard";/);
    });

    test("提交申请逻辑会阻止关闭岗位、重复申请和超过三次申请", () => {
      assert.match(source, /!"Open"\.equals\(job\.getStatus\(\)\)/);
      assert.match(source, /countApplicationsForTa\(taId\) >= MAX_APPLICATIONS/);
      assert.match(source, /You have already applied for this job\./);
    });

    test("录取更新逻辑会阻止 TA 超过 workload 上限", () => {
      assert.match(source, /"Accepted"\.equals\(status\)/);
      assert.match(source, /countAcceptedJobsForTa\(application\.getTaId\(\)\) >= MAX_ACCEPTED_JOBS/);
      assert.match(source, /Acceptance would exceed the TA workload cap\./);
    });

    test("通知逻辑会提示 TA 剩余可申请岗位数量", () => {
      assert.match(source, /You can still apply for /);
      assert.match(source, /MAX_APPLICATIONS - countApplicationsForTa\(taId\)/);
    });
  });
}

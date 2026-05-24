import assert from "node:assert/strict";
import { readProjectFile, readProjectJson } from "../../helpers/projectFiles.mjs";

export default function registerStorageSeedDataTests(runner) {
  runner.suite("后端契约测试：存储层与种子数据", ({ before, test }) => {
    let storageSource;
    let accounts;
    let jobs;
    let applications;
    let profiles;

    before(async () => {
      [
        storageSource,
        accounts,
        jobs,
        applications,
        profiles
      ] = await Promise.all([
        readProjectFile(
          "src",
          "main",
          "java",
          "com",
          "group91",
          "tars",
          "storage",
          "JsonDataStore.java"
        ),
        readProjectJson("data", "accounts.json"),
        readProjectJson("data", "job-postings.json"),
        readProjectJson("data", "applications.json"),
        readProjectJson("data", "ta-profiles.json")
      ]);
    });

    test("存储初始化会创建 data 和 uploads/cv 目录", () => {
      assert.match(storageSource, /Files\.createDirectories\(dataDirectory\)/);
      assert.match(storageSource, /Files\.createDirectories\(uploadDirectory\)/);
    });

    test("账号种子数据包含 TA、MO、Admin 三个演示账号", () => {
      assert.equal(accounts.length, 3);
      assert.deepEqual(
        accounts.map((item) => item.username).sort(),
        ["admin.demo", "mo.demo", "ta.demo"]
      );
      assert.deepEqual(
        accounts.map((item) => item.role).sort(),
        ["ADMIN", "MO", "TA"]
      );
    });

    test("岗位种子数据同时包含开放和关闭状态", () => {
      const openJobs = jobs.filter((item) => item.status === "Open");
      const closedJobs = jobs.filter((item) => item.status === "Closed");

      assert.ok(openJobs.length >= 1, "至少需要一个 Open 岗位用于 TA 浏览和申请。");
      assert.ok(closedJobs.length >= 1, "至少需要一个 Closed 岗位用于关闭岗位规则验证。");
    });

    test("申请种子数据保留 Submitted、Under Review、Accepted 的状态语义", () => {
      const statuses = new Set(applications.map((item) => item.status));

      assert.ok(statuses.has("Submitted"));
      assert.ok(statuses.has("Under Review"));
      assert.ok(statuses.has("Accepted"));
    });

    test("当前种子数据中每位 TA 的已录取岗位数量不超过 3", () => {
      const acceptedCountByTa = new Map();

      for (const application of applications) {
        if (application.status !== "Accepted") {
          continue;
        }
        acceptedCountByTa.set(
          application.taId,
          (acceptedCountByTa.get(application.taId) || 0) + 1
        );
      }

      for (const [taId, acceptedCount] of acceptedCountByTa.entries()) {
        assert.ok(
          acceptedCount <= 3,
          `TA ${taId} 的已录取岗位数量超过了 3，当前值为 ${acceptedCount}。`
        );
      }
    });

    test("TA 档案种子数据保留本地 CV 路径，便于演示上传链路", () => {
      assert.ok(profiles.length >= 1);
      for (const profile of profiles) {
        assert.ok(
          typeof profile.cvPath === "string" && profile.cvPath.startsWith("uploads/cv/"),
          `TA ${profile.id} 缺少有效的本地 CV 路径。`
        );
      }
    });
  });
}

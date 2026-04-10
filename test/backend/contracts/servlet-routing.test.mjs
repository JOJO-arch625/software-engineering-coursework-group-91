import assert from "node:assert/strict";
import { readProjectFile } from "../../helpers/projectFiles.mjs";

export default function registerServletRoutingTests(runner) {
  runner.suite("后端契约测试：Servlet 路由与权限保护", ({ before, test }) => {
    const sources = {};

    before(async () => {
      const entries = await Promise.all([
        readProjectFile("src", "main", "java", "com", "group91", "tars", "servlet", "LoginServlet.java"),
        readProjectFile("src", "main", "java", "com", "group91", "tars", "servlet", "TaDashboardServlet.java"),
        readProjectFile("src", "main", "java", "com", "group91", "tars", "servlet", "MoReviewServlet.java"),
        readProjectFile("src", "main", "java", "com", "group91", "tars", "servlet", "AdminWorkloadServlet.java"),
        readProjectFile("src", "main", "java", "com", "group91", "tars", "servlet", "BasePageServlet.java")
      ]);

      sources.login = entries[0];
      sources.taDashboard = entries[1];
      sources.moReview = entries[2];
      sources.adminWorkload = entries[3];
      sources.basePage = entries[4];
    });

    test("登录、TA、MO、Admin 路由注解保持稳定", () => {
      assert.match(sources.login, /@WebServlet\("\/login"\)/);
      assert.match(sources.taDashboard, /@WebServlet\("\/ta\/dashboard"\)/);
      assert.match(sources.moReview, /@WebServlet\("\/mo\/review"\)/);
      assert.match(sources.adminWorkload, /@WebServlet\("\/admin\/workload"\)/);
    });

    test("BasePageServlet 要求未登录用户跳回登录页", () => {
      assert.match(sources.basePage, /Please log in before accessing the recruitment workspace\./);
      assert.match(sources.basePage, /redirect\(request, response, "\/login"\)/);
    });

    test("BasePageServlet 对越权访问进行角色校验和回跳", () => {
      assert.match(sources.basePage, /requiredRole\.equals\(currentUser\.getRole\(\)\)/);
      assert.match(sources.basePage, /does not have permission to open that page/);
      assert.match(sources.basePage, /service\.getHomePathForRole\(currentUser\.getRole\(\)\)/);
    });
  });
}

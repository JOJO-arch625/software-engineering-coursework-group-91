export const DEFAULT_BASE_URL = process.env.FRONTEND_BASE_URL || "http://127.0.0.1:8080";

export const DEMO_ACCOUNTS = {
  ta: {
    username: "ta.demo",
    password: "TaDemo123",
    homePath: "/ta/dashboard"
  },
  mo: {
    username: "mo.demo",
    password: "MoDemo123",
    homePath: "/mo/dashboard"
  },
  admin: {
    username: "admin.demo",
    password: "AdminDemo123",
    homePath: "/admin/workload"
  }
};

export function buildUrl(pathname) {
  return new URL(pathname, DEFAULT_BASE_URL).toString();
}

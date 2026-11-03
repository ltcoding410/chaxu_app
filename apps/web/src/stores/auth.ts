import { computed, ref } from "vue";
import { defineStore } from "pinia";

export interface Account { id: number; username: string; displayName: string; roles: string[] }
export class ApiError extends Error {
  constructor(message: string, public status: number) { super(message); }
}
export async function api<T>(path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = {};
  if (body !== undefined) {
    const tokenResponse = await fetch("/api/csrf", { credentials: "same-origin", cache: "no-store", signal: AbortSignal.timeout(10000) });
    if (!tokenResponse.ok || !tokenResponse.headers.get("content-type")?.includes("application/json"))
      throw new ApiError("无法连接账号服务，请确认后端已启动及 /api 代理已配置。", 503);
    const token = (await tokenResponse.json()).data;
    headers[token.headerName] = token.token;
    headers["Content-Type"] = "application/json";
  }
  let response: Response;
  try {
    response = await fetch(path, { method: body === undefined ? "GET" : "POST", headers,
      credentials: "same-origin", cache: "no-store", signal: AbortSignal.timeout(10000), ...(body === undefined ? {} : { body: JSON.stringify(body) }) });
  } catch { throw new ApiError("连接失败，请检查网络及后端服务后重试。", 503); }
  if (response.status === 204) return undefined as T;
  if (!response.headers.get("content-type")?.includes("application/json"))
    throw new ApiError("账号服务未响应，请确认后端启动且 /api 代理配置正确。", 503);
  const result = await response.json();
  if (!response.ok) throw new ApiError(result.message || "请求失败，请稍后再试。", response.status);
  return result.data as T;
}
export const useAuth = defineStore("auth", () => {
  const user = ref<Account | null>(null);
  const ready = ref(false);
  const loginNeeded = ref(0);
  const isLoggingOut = ref(false);
  let revision = 0;
  let logoutRequest: Promise<void> | undefined;
  const channel = typeof BroadcastChannel !== "undefined" ? new BroadcastChannel("chaxu-session") : null;
  function announce() {
    channel?.postMessage("changed");
    try { localStorage.setItem("chaxu-session-change", crypto.randomUUID()); } catch { /* 焦点恢复时仍会校验会话 */ }
  }
  function syncSession() { if (!isLoggingOut.value) void refresh(); }
  channel?.addEventListener("message", syncSession);
  window.addEventListener("storage", event => {
    if (!channel && event.key === "chaxu-session-change") syncSession();
  });
  window.addEventListener("focus", syncSession);
  window.addEventListener("pageshow", event => { if (event.persisted) syncSession(); });
  const isStaff = computed(() => user.value?.roles.some(role => ["ADMIN", "STAFF"].includes(role)) ?? false);
  async function refresh() {
    const current = ++revision;
    try {
      const account = await api<Account | null>("/api/auth/me");
      if (current === revision) user.value = account;
    }
    catch { if (current === revision) user.value = null; }
    finally { ready.value = true; }
  }
  async function login(username: string, password: string) {
    if (isLoggingOut.value) throw new ApiError("正在退出登录，请稍后再试。", 409);
    ++revision;
    user.value = await api<Account>("/api/auth/login", { username, password });
    ++revision;
    announce();
  }
  async function register(username: string, password: string, displayName: string) {
    await api<Account>("/api/auth/register", { username, password, displayName });
  }
  function logout(): Promise<void> {
    if (logoutRequest) return logoutRequest;
    isLoggingOut.value = true;
    ++revision;
    logoutRequest = (async () => {
      try {
        await api("/api/auth/logout", {});
        ++revision;
        user.value = null;
        announce();
      } finally {
        isLoggingOut.value = false;
        logoutRequest = undefined;
      }
    })();
    return logoutRequest;
  }
  async function shoppingAccess() {
    if (isLoggingOut.value) throw new ApiError("正在退出登录，请稍候。", 409);
    if (!user.value) { loginNeeded.value++; return false; }
    const expected = user.value.id;
    try {
      const account = await api<Account>("/api/auth/shopping-access", {});
      if (isLoggingOut.value || user.value?.id !== expected) return false;
      if (account.id !== expected) { user.value = account; throw new ApiError("当前登录账号已变化，请重新操作。", 409); }
      return user.value?.id === expected;
    }
    catch (error) {
      if (error instanceof ApiError && error.status === 401) { user.value = null; loginNeeded.value++; return false; }
      throw error;
    }
  }
  return { user, ready, isStaff, loginNeeded, isLoggingOut, refresh, login, register, logout, shoppingAccess };
});

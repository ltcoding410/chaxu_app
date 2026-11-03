import { test } from "node:test";
import assert from "node:assert/strict";
import { build } from "esbuild";

// Run the real store with controlled network responses; no backend or browser required.
const channels = [];
globalThis.window = new EventTarget();
globalThis.localStorage = { setItem() {} };
globalThis.BroadcastChannel = class {
  constructor() { this.listeners = []; channels.push(this); }
  addEventListener(_, listener) { this.listeners.push(listener); }
  postMessage() { for (const channel of channels) if (channel !== this) channel.listeners.forEach(fn => fn()); }
};
const bundled = await build({ stdin: {
  contents: 'export {useAuth} from "./apps/web/src/stores/auth.ts"; export {createPinia} from "pinia";',
  resolveDir: process.cwd(),
}, bundle: true, write: false, platform: "node", format: "esm" });
const { useAuth, createPinia } = await import(`data:text/javascript;base64,${Buffer.from(bundled.outputFiles[0].text).toString("base64")}`);
const account = { id: 1, username: "tea_user", displayName: "茶友", roles: ["CUSTOMER"] };
const json = data => new Response(JSON.stringify({ data }), { headers: { "content-type": "application/json" } });
const tick = () => new Promise(resolve => setTimeout(resolve, 0));

test("logout deduplicates requests, blocks shopping, and synchronizes another tab", async () => {
  let session = account, finish, calls = 0;
  globalThis.fetch = async path => {
    if (path === "/api/csrf") return json({ headerName: "X-CSRF", token: "test" });
    if (path === "/api/auth/me") return json(session);
    if (path === "/api/auth/logout") {
      calls++;
      await new Promise(resolve => { finish = resolve; });
      session = null;
      return new Response(null, { status: 204 });
    }
    throw new Error(`Unexpected request: ${path}`);
  };
  const first = useAuth(createPinia()), second = useAuth(createPinia());
  await first.refresh(); await second.refresh();
  const pending = first.logout(), duplicate = first.logout();
  await tick();
  assert.equal(calls, 1);
  assert.equal(first.isLoggingOut, true);
  await assert.rejects(first.shoppingAccess(), /正在退出/);
  finish();
  await Promise.all([pending, duplicate]); await tick();
  assert.equal(first.user, null);
  assert.equal(second.user, null);
  assert.equal(first.isLoggingOut, false);
});

test("failed logout preserves account and can be retried", async () => {
  let fail = true;
  globalThis.fetch = async path => {
    if (path === "/api/csrf") return json({ headerName: "X-CSRF", token: "test" });
    if (path === "/api/auth/me") return json(account);
    if (fail) throw new Error("offline");
    return new Response(null, { status: 204 });
  };
  const auth = useAuth(createPinia()); await auth.refresh();
  await assert.rejects(auth.logout());
  assert.equal(auth.user.id, 1);
  assert.equal(auth.isLoggingOut, false);
  fail = false; await auth.logout();
  assert.equal(auth.user, null);
  await tick();
});

test("a stale session refresh cannot restore an account after logout", async () => {
  let finish;
  globalThis.fetch = async path => {
    if (path === "/api/csrf") return json({ headerName: "X-CSRF", token: "test" });
    if (path === "/api/auth/me") return new Promise(resolve => { finish = () => resolve(json(account)); });
    return new Response(null, { status: 204 });
  };
  const auth = useAuth(createPinia()); auth.user = account;
  const refresh = auth.refresh();
  // Suppress other stores' refresh in this test so the deferred response belongs to this store.
  const listeners = channels.splice(0);
  await auth.logout(); finish(); await refresh;
  channels.push(...listeners);
  assert.equal(auth.user, null);
});

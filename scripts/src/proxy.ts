import http from "http";
import httpProxy from "http-proxy";

const PROXY_PORT = Number(process.env.PORT ?? 3000);
const FRONTEND_PORT = 5000;
const BACKEND_PORT = 8080;

const DEV_PREFIX = "/__dev-frontend";

const proxy = httpProxy.createProxyServer({});

proxy.on("error", (err, _req, res) => {
  if (res instanceof http.ServerResponse) {
    res.writeHead(502, { "Content-Type": "text/plain" });
    res.end(`Proxy error: ${(err as Error).message}`);
  }
});

const server = http.createServer((req, res) => {
  const raw = req.url ?? "/";

  // Strip the frontend base-path prefix so /api and /ws are always recognised
  const url = raw.startsWith(DEV_PREFIX)
    ? raw.slice(DEV_PREFIX.length) || "/"
    : raw;

  if (url.startsWith("/api") || url.startsWith("/ws")) {
    req.url = url; // rewrite before forwarding
    proxy.web(req, res, { target: `http://localhost:${BACKEND_PORT}` });
  } else {
    req.url = raw; // keep original path for Vite
    proxy.web(req, res, { target: `http://localhost:${FRONTEND_PORT}` });
  }
});

server.on("upgrade", (req, socket, head) => {
  proxy.ws(req, socket, head, { target: `http://localhost:${FRONTEND_PORT}` });
});

server.listen(PROXY_PORT, "0.0.0.0", () => {
  console.log(`KAYA proxy running on port ${PROXY_PORT}`);
  console.log(`  → /api -> localhost:${BACKEND_PORT}`);
  console.log(`  → /*   -> localhost:${FRONTEND_PORT}`);
});

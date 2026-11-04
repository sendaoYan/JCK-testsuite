# java.net.http.WebSocket 设计文档

## 1. API 功能

WebSocket 客户端接口，及其嵌套 `Builder`/`Listener`。

- 常量：`NORMAL_CLOSURE`（=1000）。
- 发送：`sendText(CharSequence, boolean)`、`sendBinary(ByteBuffer, boolean)`、`sendPing(ByteBuffer)`、
  `sendPong(ByteBuffer)`、`sendClose(int, String)`（均返回 `CompletableFuture<WebSocket>`）。
- 查询/控制：`request(long)`、`getSubprotocol()`、`isOutputClosed()`、`isInputClosed()`、`abort()`。
- `Builder`：`header`、`connectTimeout`、`subprotocols`、`buildAsync(URI, Listener)`（真正握手，需网络）。
- `Listener`：`onOpen/onText/onBinary/onPing/onPong/onClose/onError`（默认方法调用 `webSocket.request(1)` 或 no-op）。

## 2. Java 用户如何使用

- 组合链路：`HttpClient.newHttpClient().newWebSocketBuilder().header(...).buildAsync(uri, listener)`。
- 本地可测：builder 的 `header/connectTimeout/subprotocols` 为流式返回 this；`Listener` 默认方法可用 mock WebSocket 调用。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.net.http/share/classes/java/net/http/WebSocket.java` 与
  `jdk/internal/net/http/websocket/BuilderImpl.java`）：`header/connectTimeout/subprotocols` 返回 this；
  `Listener` 默认方法（除 `onError`/`onClose`）调用 `webSocket.request(1)`。

## 4. 测试用例设计 + 自动化实现

文件：`WebSocketTest.java`（testWhiteBox + testBlackBox）。

- 白盒：interface / `NORMAL_CLOSURE==1000` / 发送与控制方法面 / 嵌套 Builder、Listener 接口。
- 黑盒（不真发请求）：`newWebSocketBuilder()` 流式方法返回 this + null 参数异常 →
  用 `Proxy` mock 的 WebSocket 驱动 `Listener` 默认方法（返回 null / no-op 不抛）。

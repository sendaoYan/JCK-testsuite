# java.net.http.WebSocket.Listener 设计文档

## 1. API 功能

WebSocket 事件回调接口，七个 default 方法：onOpen(void)、onText/onBinary/onPing/onPong/onClose
（返回 CompletionStage<?>）、onError(void)。

## 2. 使用方式

`client.newWebSocketBuilder().buildAsync(uri, listener)`；部分覆盖即可，其余继承 default。

## 3. 参考来源

- JDK-27 `javap`；`WebSocket.java` 规范（"default methods request one more message"）。

## 4. 测试用例设计 + 易错规范细节

文件：`ListenerTest.java`（用 Proxy 模拟 WebSocket，零网络）。

- 形状：7 个方法全 default（probed）；精确签名（参数类型/个数/返回）；public、非 sealed、非泛型。
- 默认行为（probed）：onOpen/onText/onBinary/onPing/onPong 各调用 `webSocket.request(1)`；
  onClose/onError 不调用；CompletionStage 类 default 返回 null。
- 请求计数贯穿多次调用；子类部分覆盖继承其余；自定义 onBinary 返回受控 stage 且不自动 request；
  null/空文本、各种 close code 均不影响计数。

## 5. 版本差异

- 自 JDK 11 稳定；JDK-8 无此包。

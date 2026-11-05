# javax.xml.ws.handler.Handler — 设计文档

## 一、API 功能
`Handler<C extends MessageContext>` 是 JAX-WS 处理器框架的根接口（JSR 224 §9）。它声明 3 个方法：`handleMessage(C)`（处理正常消息，返回 false 短路处理方向）、`handleFault(C)`（处理协议故障消息）、`close(MessageContext)`（消息交换结束后释放资源，必被调用）。类型参数 `C` 上界为 `MessageContext`，衍生出 `LogicalHandler`、`SOAPHandler` 两个子通道。处理器实例由 `HandlerResolver` 按端口提供，运行时在每次调用装配成链。

## 二、使用方式
实现该接口（通常实现 `LogicalHandler<LogicalMessageContext>` 或 `SOAPHandler<SOAPMessageContext>`），通过 `Service.setHandlerResolver(...)` 注册；运行时在客户端出站方向按链序调用 `handleMessage`，入站方向反序调用；任一返回 false 则反转方向并跳过业务派发；`handleFault` 在故障路径调用；`close` 在两个方向终结后各调用一次。测试中用回环 HttpServer + `Provider<Source>`(PAYLOAD) 端点 + `Dispatch` 建立真实闭环。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.handler.Handler（Java EE 7 / JDK 8 附带 RI）
- JSR 224 (JAX-WS 2.2) 规范第 9 章 Handler Framework
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.handler.*
- 本仓库测试：JDK-8/API/javax/xml/ws/handler/Handler/HandlerTest.java

## 四、测试用例设计 + 易错规范细节
- 接口面：恰 3 个声明方法（handleMessage/handleFault/close）；类型变量名 C 且上界 MessageContext；close 参数类型为原始 MessageContext（擦除后 getMethod 需用 MessageContext.class 查找）。
- 真实通道断言：一次 invoke → 处理器 handleMessage 2 次（出+入）、close 1 次、provider 命中 1 次。
- 处理器抛 RuntimeException → 客户端收到 SOAPFaultException，faultstring 含异常消息；provider 不被调用。
- handleMessage 返回 false → 下游阻断（provider 不调用），invoke 仍能返回出站消息。
- 易错点：(1) 泛型接口的方法反射查找必须用擦除参数类型；(2) 处理器回调内快照上下文数据（上下文在方向切换时被复用/清空）；(3) 每个发布的 Endpoint 必须 stop，否则非守护 HttpServer Dispatcher 线程导致 JVM 挂起。

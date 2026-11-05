# javax.xml.ws.handler.LogicalHandler — 设计文档

## 一、API 功能
`LogicalHandler<C extends LogicalMessageContext>` 是处理器框架的"消息载荷"通道：处理器只看到 XML 载荷（Source/JAXB bean），看不到 SOAP 信封/头等协议细节。它自身不声明任何方法（标记接口），全部行为继承自 `Handler`，仅把类型参数收窄到 `LogicalMessageContext`，与 `SOAPHandler`（全消息通道）相对。

## 二、使用方式
实现 `LogicalHandler<LogicalMessageContext>` 三个继承方法（handleMessage/handleFault/close），经 HandlerResolver 或 Binding.setHandlerChain 装配。通过 `LogicalMessageContext.getMessage().getPayload()/setPayload()` 读写/改写载荷。测试用自造假上下文验证语义，再用回环端点验证运行时传入真实 LogicalMessageContext。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.handler.LogicalHandler
- JSR 224 §9.3 Logical Handlers
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.handler.LogicalHandler
- 本仓库测试：JDK-8/API/javax/xml/ws/handler/LogicalHandler/LogicalHandlerTest.java

## 四、测试用例设计 + 易错规范细节
- 接口面：0 个自有声明方法（纯标记）；直接父接口 Handler；类型变量名 C 上界 LogicalMessageContext；不含 getHeaders（区别于 SOAPHandler）。
- 反射查找 handleMessage 必须用擦除类型 MessageContext.class（泛型 C 擦除结果）。
- 真实通道：一次 invoke → handleMessage 2 次（出站 TRUE、入站 FALSE，方向标志在回调内捕获）、close 1 次；第二次 invoke 生命周期完整重走。
- 自造实现：payload Source 透过假上下文回读；handleFault/close 计数。
- 易错点：方向标志必须在 handleMessage 内部读取（上下文复用）；测试计数器为 static，跨段使用前要清零。

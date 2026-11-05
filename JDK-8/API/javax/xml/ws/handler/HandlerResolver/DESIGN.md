# javax.xml.ws.handler.HandlerResolver — 设计文档

## 一、API 功能
`HandlerResolver` 只有一个方法 `getHandlerChain(PortInfo)`：运行时在为某端口建立处理器链时（如 createDispatch/addPort）咨询它，返回该端口的 `List<Handler>`。它把"哪些处理器、什么顺序"的配置权从代码化的 Service 上解耦出来，是 JSR 224 §9.2.1 的注册点。

## 二、使用方式
`service.setHandlerResolver(resolver)`；之后创建 Dispatch/代理时运行时按 PortInfo（服务名/端口名/绑定 ID）询问链。返回空 List 合法（无处理器）。测试以自造 Resolver 记录咨询次数与收到的 PortInfo，并在回环端点上用 Dispatch 驱动真实链路。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.handler.HandlerResolver
- JSR 224 §9.2.1 HandlerResolver
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.handler.HandlerResolverImpl
- 本仓库测试：JDK-8/API/javax/xml/ws/handler/HandlerResolver/HandlerResolverTest.java

## 四、测试用例设计 + 易错规范细节
- 接口面：恰 1 个声明方法 getHandlerChain(PortInfo) 返回 java.util.List。
- 默认 resolver 为 null（未设置时 Service.getHandlerResolver()）；设置后身份可回读。
- 真实通道：resolver 在 addPort 时即被咨询（实测，至少 1 次）；每次新建 Dispatch 会再次咨询——断言用动态计数（>=），不写死次数。
- 链序：出站 Logical 在前 SOAP 在后；入站反序（记录方向标志验证）。
- 空 List 链：invoke 正常成功（无处理器也闭环）。
- 易错点：咨询时机是 RI 行为（addPort/createDispatch），规范只承诺"建链时"；断言须宽松为次数下界而非精确值。

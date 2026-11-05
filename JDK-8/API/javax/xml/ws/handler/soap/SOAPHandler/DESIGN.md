# javax.xml.ws.handler.soap.SOAPHandler — 设计文档

## 一、API 功能
`SOAPHandler<T extends SOAPMessageContext>` 是处理器框架的"全消息"通道：处理器可见完整 SOAPMessage（信封+头+体）。它自有唯一方法 `getHeaders()` 返回 `Set<QName>`——告知运行时本处理器理解的 SOAP 头，运行时据此构建 mustUnderstand 头的处理证明。

## 二、使用方式
实现 getHeaders + 三个继承方法，经 HandlerResolver/Binding.setHandlerChain 装配。运行时在建链时咨询 getHeaders（注册理解头集）；handleMessage 里经 SOAPMessageContext.getMessage() 拿 SAAJ SOAPMessage 读写头/体。测试覆盖接口面、自造生命周期、真实链路的咨询时机与方向计数。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.handler.soap.SOAPHandler
- JSR 224 §9.4.1 SOAP Handlers
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.handler.SOAPHandler
- 本仓库测试：JDK-8/API/javax/xml/ws/handler/soap/SOAPHandler/SOAPHandlerTest.java

## 四、测试用例设计 + 易错规范细节
- 接口面：恰 1 个自有声明方法 getHeaders() 返回 java.util.Set；直接父接口 Handler。
- 真实链路：getHeaders 在链建立时被咨询（>=1 次），同一 Dispatch 再 invoke 不重复咨询（实测计数不变）；handleMessage 出+入共 2 次；close 每次 invoke 1 次。
- 易错点：(1) invoke 返回后调 lastContext.getMessage() 会因底层流已关闭抛 XMLStreamException——消息读取必须在 handleMessage 内（实测记录）；(2) static 计数器在进入真实链路段前要清零，避免自造段污染。

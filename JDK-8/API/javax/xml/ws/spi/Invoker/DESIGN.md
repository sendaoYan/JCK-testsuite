# javax.xml.ws.spi.Invoker — 设计文档

## 一、API 功能
`Invoker`（public abstract，公共无参构造器）是 JSR 224 §5.5 的服务端挂钩：容器/扩展用它控制 WebServiceContext 注入与方法派发。恰 2 个抽象方法：`inject(WebServiceContext)` 与 `invoke(Method, Object...)`，各声明恰 3 个受检异常（IllegalAccessException/IllegalArgumentException/InvocationTargetException）。

## 二、使用方式
子类持有目标实例；inject 保存运行时给的 WebServiceContext（供 @Resource 注入）；invoke 用反射把容器解析出的方法与参数派发到实例并返回结果，目标方法异常按反射约定包成 InvocationTargetException。测试覆盖精确 throws 子句、子类生命周期（注入身份/派发/装箱/void 返回 null）、异常包装。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.spi.Invoker
- JSR 224 §5.5 Invoker
- OpenJDK 8 内置 RI: com.sun.xml.internal.ws.server.InvokerInvocationException 消费端
- 本仓库测试：JDK-8/API/javax/xml/ws/spi/Invoker/InvokerTest.java

## 四、测试用例设计 + 易错规范细节
- throws 面精确到 3 类型×2 方法（多一个少一个都算失败）——这是容器契约，编译器强制子类兼容。
- 子类生命周期：inject 记录调用与 ctx 身份；invoke 派发 echo/add/noArg（void → null，int 参数自动装箱）；invoke 先于 inject 也能工作（顺序由框架保证）。
- 异常包装：目标抛 IllegalStateException → InvocationTargetException，cause 保留原消息。
- 最小 WebServiceContext 假实现需实现全部方法（含 2 个 getEndpointReference 重载）——编译器要求。
- 易错点：子类 invoke 的 throws 必须逐字声明三类异常，写 throws Exception 反而编译失败（不能拓宽）。

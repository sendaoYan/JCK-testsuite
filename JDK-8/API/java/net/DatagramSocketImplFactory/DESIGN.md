# DatagramSocketImplFactory 设计文档（JDK-8）

## 一、API 功能

单抽象方法工厂接口：`DatagramSocketImpl createDatagramSocketImpl()`。经
`DatagramSocket.setDatagramSocketImplFactory` 一次性注入 JVM（每 JVM 恰一次）。

## 二、Java 用户如何使用

实现（匿名类/lambda 均可，SAM 接口）并注册；此后 DatagramSocket 构造时咨询工厂获取 impl。

## 三、参考来源

- Oracle Java SE 8 javadoc（java/net/DatagramSocketImplFactory.html、DatagramSocket.setDatagramSocketImplFactory）。
- 双 JDK 探针实测（/tmp/probe-g，2026-11-05）。

## 四、测试用例设计（DatagramSocketImplFactoryTest.java，31 断言）

| 方法 | 覆盖 |
|---|---|
| testLegacy | 保留原断言（接口/无父类/方法存在） |
| testWhiteBox | 公开接口、无父接口、恰好 1 个声明方法、返回 DatagramSocketImpl、0 参数、public abstract、无受检异常、无声明字段、包 java.net、安装器签名 |
| testImplementations | 匿名类返回 null、lambda 返回 null、Stub 工厂返回自定义 impl（15 抽象成员全实现）：类型/共享实例/二次一致 |
| testMethodContract | 工厂抛 RuntimeException 原样透传（同一异常对象）；计数工厂每次调用产出新实例 |
| testFactoryLifecycle（文件尾，JVM 全局） | 首次 setDatagramSocketImplFactory 成功；new DatagramSocket(0) 咨询工厂（计数≥1）、null 返回 → NPE（JDK-8 消息 "null"）；二次 set（含 null 实参）→ SocketException "factory already defined"（精确消息，双侧一致） |

## 五、易错规范细节

- factory 是 JVM 全局一次性状态：相关断言必须放文件末尾，且其后的测试不能再创建 DatagramSocket。
- null 返回的 NPE 消息：JDK-8 "null" vs JDK-27 "Implementation returned by installed DatagramSocketImplFactory is null" → 双侧仅断类型。

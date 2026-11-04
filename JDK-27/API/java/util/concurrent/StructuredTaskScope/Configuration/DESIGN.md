# java.util.concurrent.StructuredTaskScope$Configuration 设计案

## 1. API 功能
结构化并发作用域配置接口（预览）。`withThreadFactory`/`withName`/`withTimeout` 流式配置。

## 2. Java 用户如何使用
`StructuredTaskScope.open(UnaryOperator<Configuration>)` 中链式设置名称、超时、线程工厂。

## 3. 开源代码实际用法调研
JDK 内部：`java.util.concurrent.StructuredTaskScope.open(UnaryOperator)` 传入配置；`test/jdk/java/util/concurrent/structured/` 覆盖。

## 4. 测试用例设计 + 自动化实现
场景：
1. 白盒：接口 + withThreadFactory/withName/withTimeout 三 wither。
2. 黑盒主干：fluent 链用于 open。
3. 契约深化：wither 返回**新实例**（不可变 wither 语义、不同 wither 互不相同）、open 传入默认 Configuration
   （testWitherSemantics）；自定义线程工厂 → 平台线程 + 工厂命名线程（testThreadFactoryConfiguration）；
   短超时真实触发（ExecutionException cause CancelledByTimeoutException、isCancelled）与宽超时不扰动
   （testTimeoutConfiguration）。

实现：`ConfigurationTest.java`（14 断言）。

## 关键规范细节（实测锁定）
- wither 均返回新 Configuration 实例（cfg == cfg.withName(x) 为 false）。
- withName 命名的是 scope，不改变子任务线程名（子任务线程名断言只对 withThreadFactory 做）。

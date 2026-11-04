# Runtime — 设计方案

## 1. API 功能
`java.lang.Runtime`（final，单例）提供 JVM 运行时环境：
- 资源：`availableProcessors`、`totalMemory/freeMemory/maxMemory`
- 进程：`exec`（多种重载）、`halt/exit`
- 其他：`gc/runFinalization/load/loadLibrary/addShutdownHook/removeShutdownHook`
- 版本：静态 `version()` 返回 `Runtime.Version`

## 2. Java 用户如何使用
- `Runtime.getRuntime().availableProcessors()` 决定线程池大小。
- `Runtime.version()` 获取运行时特性版本。
```java
int cores = Runtime.getRuntime().availableProcessors();
Runtime.Version v = Runtime.version();  // 27.0.1
```

## 3. 开源代码实际用法调研
- `Executors.newFixedThreadPool` 等以 `Runtime.getRuntime().availableProcessors()` 为默认线程数。
- `System.exit` 内部委托 `Runtime.getRuntime().exit`。
- JDK `test/jdk/java/lang/Runtime` 系列验证内存关系、`version`、`exec`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Runtime")` 验证 final、无公开构造器、`getRuntime` static。
黑盒：单例；`availableProcessors >= 1`；`maxMemory >= totalMemory >= freeMemory >= 0`；`version()` 非空；`gc/runFinalization` 不抛。不调用 `halt/exit`。
对应 `<Simple>Test.java` 黑盒部分。

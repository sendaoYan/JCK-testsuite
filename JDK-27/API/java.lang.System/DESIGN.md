# System — 设计方案

## 1. API 功能
`java.lang.System`（final，不可实例化）提供系统级工具：
- 标准流：`in/out/err` 字段 + `setIn/setOut/setErr`
- 数组：`arraycopy`；标识：`identityHashCode`
- 属性：`getProperties/getProperty/setProperty/clearProperty`；环境：`getenv`
- 时间：`currentTimeMillis/nanoTime`；行分隔符：`lineSeparator`
- 其他：`gc/runFinalization/load/loadLibrary/mapLibraryName/getLogger`

## 2. Java 用户如何使用
- `System.arraycopy` 高效复制数组；`System.getProperty("user.home")` 读配置。
- `System.currentTimeMillis()` 测墙钟，`System.nanoTime()` 测单调耗时。
```java
long t0 = System.nanoTime();
doWork();
long elapsed = System.nanoTime() - t0;
```

## 3. 开源代码实际用法调研
- `System.getProperty` 是 Spring/Log4j 读取 `user.home`、`java.version` 的标准方式。
- `System.arraycopy` 是 `ArrayList`/`StringBuilder` 内部扩容的核心原语。
- JDK `test/jdk/java/lang/System` 系列验证 `arraycopy` 重叠/类型不匹配、属性读写、`identityHashCode`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.System")` 验证 final、非接口、无公开构造器、`in/out/err` 字段存在。
黑盒：`arraycopy` 全量/部分/重叠/长度 0/类型不匹配 + NPE/IOOBE/ASE；`java.version`/`os.name` 非空；唯一键属性 set/get/clear 后恢复；`getenv` 可调用；`identityHashCode`；`lineSeparator`；时间单调；`getLogger`/`mapLibraryName`；`gc/runFinalization` 不抛。
对应 `<Simple>Test.java` 黑盒部分。

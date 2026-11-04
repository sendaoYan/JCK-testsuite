# java.lang.Runtime 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「getRuntime 读一下」的冒烟。重点补：白盒结构（JDK-8 非 final、
> 非接口、无公开构造器、getRuntime static）、单例、availableProcessors、内存关系
> （max>=total>=free>=0）、gc/runFinalization 不抛、version() 反射探测（JDK-8 无该方法，Java 9+ 才有）、
> shutdown hook 增删/重复 IAE/null NPE、exec 缺失命令抛 IOException、load/loadLibrary 缺失库抛
> UnsatisfiedLinkError。断言确定，headless 安全，不调用 exit/halt。

## 1. API 提供了哪些功能

- **类**：`java.lang.Runtime`（JDK-8 非 final，Java 9+ 才 final），单例（`getRuntime()`，private 构造）。
- **资源**：`availableProcessors`、`totalMemory`/`freeMemory`/`maxMemory`。
- **进程**：`exec`（6 个重载）、`exit`/`halt`（测试不调用）。
- **其他**：`gc`/`runFinalization`/`load`/`loadLibrary`/`addShutdownHook`/`removeShutdownHook`。
- **版本**：`version()` 是 Java 9+，JDK-8 无（用反射探测）。

## 2. Java 用户如何使用

- `Runtime.getRuntime().availableProcessors()` 决定线程池大小；`System.exit` 内部委托 `Runtime.exit`。
- `addShutdownHook` 注册 JVM 退出钩子（同一 hook 注册两次抛 `IllegalArgumentException`）。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Runtime.html`）：内存关系、exec、shutdown hook、load。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Runtime.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：`Runtime` 非 final。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Runtime`。

## 4. 测试用例设计（映射到 RuntimeTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：非 final、非接口、无公开构造器、getRuntime static | testWhiteBox |
| P0 | 单例 | testSingleton |
| P0 | availableProcessors >= 1 | testProcessors |
| P0 | maxMemory >= totalMemory >= freeMemory >= 0 | testMemory |
| P1 | gc/runFinalization 不抛 | testGc |
| P1 | version() 反射探测（JDK-8 缺省用 java.version 属性） | testVersion |
| P0 | shutdown hook：增删往返、重复 IAE、remove 未注册 false、add null NPE | testShutdownHook |
| P1 | exec 缺失命令抛 IOException（6 重载）、load/loadLibrary 缺失库抛 ULE | testExecAndLoad |

## 关键规范细节（易错，已锁入断言）

- **JDK-8 `Runtime` 非 final**（Java 9+ 才 final）。
- **`addShutdownHook` 同一 hook 注册两次抛 `IllegalArgumentException`；null 抛 `NullPointerException`**；
  `removeShutdownHook` 未注册返回 false。
- **`exec` 缺失命令抛 `IOException`**（在真正 spawn 进程之前）；不调用 `exit`/`halt`（会终止 JVM）。
- **`load`/`loadLibrary` 缺失库抛 `UnsatisfiedLinkError`**。
- **`version()` 仅 Java 9+**：JDK-8 用反射探测，缺省时用 `java.version` 属性兜底。

## 注意事项

- 不调用 exit/halt/System.exit；exec 只用缺失命令（不真正启动进程）；load 只用不存在的库路径。
- headless 安全，不加 `@key`；结果确定。

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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）

- **表面反射枚举**：21 个可解析方法（含 `traceInstructions/traceMethodCalls` 仅 8 侧存在）；`version()` 在 JDK-8 不存在（`NoSuchMethodException` 锁定）；6 个 `exec` 重载返回类型均为 `Process`；`getRuntime` 为 static native。
- **单例契约**：main 线程、静态初始化、daemon 线程、spawned 进程 6 个调用点同一实例。
- **内存不变式**：16MB ballast 下 `totalMemory >= ballast`、`max >= total >= free >= 0`。
- **shutdown hooks 全矩阵**：`removeShutdownHook(null)` NPE；同一 Thread 注册两次 **IAE**（实测，非 ISE）；对已启动线程 add **IAE "Hook already running"**（精确消息）；已结束线程再 add 合法；hook 注册/移除后保持 NEW 状态；同一 Runnable 构造的两个 Thread 是不同 hook。
- **exec 负例**：`exec("")` IAE "Empty command"（精确）；`exec("   ")` 与 `exec(new String[0])` 均抛 `ArrayIndexOutOfBoundsException`（非 IOException）。
- **exec 真进程**：spawn `java -version` → exitValue 0、stderr 含 "version"。
- **load/loadLibrary 负例**：与 SystemTest 相同的精确消息族。
- **traceInstructions/traceMethodCalls**：8 侧可调用 no-op。

### 2. 使用方式

```java
Runtime r = Runtime.getRuntime();
Thread hook = new Thread(() -> {});
r.addShutdownHook(hook);   // 重复注册抛 IllegalArgumentException
r.removeShutdownHook(hook); // true；再 remove 返回 false
```

### 3. 参考来源

- 实测 Temurin 8u372（探针 /tmp/lcF）：hook 状态矩阵、exec 负例类型、消息文本全部先实测。
- Oracle Java SE 8 javadoc（Runtime shutdown hooks/exec/load）。
- javap 双侧枚举对照（trace 方法 27 侧已删除）。

### 4. 测试用例设计（映射到 RuntimeTest.java 新增方法）+ 易错规范细节

| 测试点 | 用例方法 |
|---|---|
| 21 方法反射表面、version() 缺席、native 性 | testSurfaceReflection |
| 6 调用点单例 | testSingletonContract |
| processors >= 1 | testProcessorsContract |
| 16MB ballast 内存不变式 | testMemoryContract |
| gc/runFinalization 可调用 | testGcContract |
| equals/hashCode/toString/非 Serializable | testObjectSemantics |
| hook 状态机全矩阵 | testShutdownHooksContract |
| exec 负例（IAE/AIOOBE） | testExecNegatives |
| java -version 真进程 | testExecReal |
| load 族负例 | testLoadNegatives |
| trace 方法 no-op | testTraceMethods |

- **易错**：**double addShutdownHook = IllegalArgumentException**（设计文档此前误记 ISE，已实测纠正）。
- **易错**：真 exec 进程必须**先读完输出再 destroy**——destroy 后读流抛 "Stream closed"。
- **易错**：`exec("")` 是 IAE "Empty command"，而全空白串是 AIOOBE——类型不同。
- **易错**：JDK-8 `Runtime` 非 final（27 侧 final，`instanceof Serializable` 在 27 不合法，用 `isAssignableFrom`）。

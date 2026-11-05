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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）

- **Runtime.Version 契约（27 侧独有）**：`version().feature()==27`、`major()==27`、`interim()==0`、`update()==1`（构建 27.0.1.0.2，实测锁定）、`patch()==0`；`Runtime.version()` 与 `Version.parse(version().toString())` 相等；compareTo/compareToIgnoreOptional/equals/hashCode 自洽。
- **表面反射**：`traceInstructions/traceMethodCalls` 在 27 侧**已删除**（`NoSuchMethodException` 锁定）；`version()` 存在且 static；6 个 exec 重载。
- **shutdown hooks 矩阵**：remove(null) NPE、double-add **IAE**（实测非 ISE）、已启动线程 add IAE "Hook already running"（精确）、已结束线程 add 合法、hook 保持 NEW。
- **exec 负例/真进程**：`exec("")` IAE "Empty command"；空白串/空数组 AIOOBE；`java -version` exit 0。
- **load 族负例**：`no X in java.library.path: <pathlist>` 前缀匹配（27 追加路径列表）。
- **final 类**：`Runtime` 在 27 侧 final（8 侧非 final）——`instanceof Serializable` 编译不过，用 `!Serializable.class.isAssignableFrom(Runtime.class)`。

### 2. 使用方式

```java
Runtime.Version v = Runtime.version();
assert v.feature() == 27;          // $FEATURE
assert Version.parse(v.toString()).equals(v);
```

### 3. 参考来源

- 实测 Temurin jdk-27.0.1.0.2+1（探针 /tmp/lcF）：feature/interim/update/patch 全字段实测后锁。
- JEP 322（Version 方案 $FEATURE/$INTERIM/$UPDATE/$PATCH）与 JDK-27 javadoc。
- javap 双侧对照（trace 方法删除、final 化）。

### 4. 测试用例设计（映射到 RuntimeTest.java 新增方法）+ 易错规范细节

| 测试点 | 用例方法 |
|---|---|
| Version 全字段 + parse 往返 + 比较 | testVersionContract |
| 表面反射（trace 已删、version 存在） | testSurfaceReflection |
| 单例 6 调用点 | testSingletonContract |
| processors/内存不变式（16MB ballast） | testProcessorsContract / testMemoryContract |
| gc 可调用 | testGcContract |
| 非 Serializable（final 类用 isAssignableFrom） | testObjectSemantics |
| hook 状态机全矩阵 | testShutdownHooksContract |
| exec 负例（IAE/AIOOBE） | testExecNegatives |
| java -version 真进程（先读流后 destroy） | testExecReal |
| load 族负例（27 消息形） | testLoadNegatives |

- **易错**：`update()` 依构建号而变（27.0.1 → update==1），锁值时必须注明构建来源。
- **易错**：final 类上的 `instanceof` 非法是**编译期**错误，双侧兼容写法用反射。
- **易错**：`double addShutdownHook` 实测为 IAE 而非 ISE；真 exec 先读流再 destroy。

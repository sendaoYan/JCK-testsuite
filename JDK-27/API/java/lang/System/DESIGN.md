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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）

- **arraycopy 完整矩阵**：8 基本类型 + 引用数组的 8 组重叠窗口快照、同数组别名、二维行拷贝、`String[]→Object[]` 协变、`len==0` 越界合法、MIN/MAX 长度负例。
- **arraycopy 异常消息精确锁定（JDK-27 有文本，JDK-8 为 null）**：`arraycopy: length -1 is negative`；`arraycopy: source index -1 out of bounds for int[3]`；`arraycopy: last source index 4 out of bounds for int[3]`；destination 两种同构；ASE `arraycopy: element type mismatch: can not cast one of the elements of java.lang.Object[] to the type of the destination array, java.lang.String`；类型族 `arraycopy: type mismatch: can not copy int[] into object array[]`（及反向）。
- **properties 活表 / getenv 不可变视图 / identityHashCode / lineSeparator**：与 8 侧同套契约。
- **SecurityManager 移除（JEP 486）**：`getSecurityManager()==null`。
- **load/loadLibrary 负例**：`Can't load library: <path>`、`Expecting an absolute path...`、`Directory separator should not appear in library name: a/b` 精确；`no X in java.library.path: <pathlist>`（比 8 侧多路径列表，前缀匹配）；27 侧对 `System::load` 会输出 restricted-method WARNING（jtreg 下无害）。

### 2. 使用方式

```java
int[] a = {1,2,3};
System.arraycopy(a, 0, a, 1, 3);
// 越界时: IndexOutOfBoundsException: arraycopy: last source index 4 out of bounds for int[3]
```

### 3. 参考来源

- 实测 Temurin jdk-27.0.1.0.2+1（探针 /tmp/lcF）：全部消息文本先实测再锁。
- JDK-27 javadoc（System/arraycopy/JEP 486）。
- 与 JDK-8 侧同探针对照所得差异清单已锁入断言注释 `[JDK27]`/`[JDK8]`。

### 4. 测试用例设计（映射到 SystemTest.java 新增方法）+ 易错规范细节

| 测试点 | 用例方法 |
|---|---|
| 重叠矩阵/别名/2D/协变 | testArraycopyMatrix |
| 5 形态 IOOBE 精确消息 + ASE 时序/消息 | testArraycopyExceptions |
| 边界长度负例 | testArraycopyEdges |
| 时间弱契约 | testTimeWeak |
| 活表 + 24 标准键 | testPropertiesContract |
| PATH/视图身份/4 UOE | testEnvContract |
| identityHashCode 契约 | testIdentityHashCodeContract |
| getSecurityManager()==null [JEP 486] | testSecurityManagerRemoved |
| load 族负例（27 消息形） | testLoadNegatives |
| mapLibraryName/流/flush | testMiscSurface |

- **易错**：27 侧字符串拼接 + `.equals` 优先级——拼接必须加括号再比较。
- **易错**：`loadLibrary` 消息含完整 `java.library.path`，必须用 `startsWith` 而非全等。
- **易错**：活表/env 变更必须在 finally 还原；不 setSecurityManager（27 侧已移除且测试绝不调用）。

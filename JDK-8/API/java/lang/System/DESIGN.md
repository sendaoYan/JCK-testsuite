# java.lang.System 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「println 读一下」的冒烟。重点补：标准流非空、arraycopy
> （全量/部分/同数组重叠右移/左移/长度 0/引用数组 Object[]→String[]/null NPE/越界 IOOBE/元素类型不匹配
> 与 int[]→long[] 的 ASE）、properties（getProperties/getProperty 带缺省/setProperty 返回旧值/
> clearProperty 返回旧值）、环境变量 map 与缺失 null、identityHashCode（稳定/默认 hashCode/ null==0）、
> lineSeparator（与 line.separator 属性一致）、时间 API（currentTimeMillis 合理范围、nanoTime 非负单调）、
> gc/runFinalization 可调用。断言确定，headless 安全，不调用 exit/halt。

## 1. API 提供了哪些功能

- **类**：`java.lang.System`（final、无公开构造器，全是 static 方法）。
- **标准流**：`in`/`out`/`err`。
- **数组**：`arraycopy`（native，处理重叠与类型检查）。
- **属性**：`getProperties`/`getProperty`/`setProperty`/`clearProperty`/`getenv`（map 与单键）。
- **标识/其他**：`identityHashCode`/`lineSeparator`/`gc`/`runFinalization`。
- **时间**：`currentTimeMillis`/`nanoTime`。
- **JDK-8 无**（Java 9+）：`getLogger`/`Logger` 家族（`System.Logger`）。

## 2. Java 用户如何使用

- `System.arraycopy(src, i, dst, j, n)` 拷贝数组（支持同数组重叠）。
- `System.setProperty/clearProperty` 管理 JVM 属性；`System.currentTimeMillis/nanoTime` 计时。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/System.html`）：arraycopy 类型检查、identityHashCode(null)==0、
  lineSeparator 与 line.separator 属性一致。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/System.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：arraycopy int[]→long[] 抛 ArrayStoreException（不拓宽）。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/System`。

## 4. 测试用例设计（映射到 SystemTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 标准流非空 | testStreams |
| P0 | arraycopy：全量/部分/重叠右移/左移/长度 0/引用数组/null NPE/越界 IOOBE/类型 ASE | testArraycopy |
| P0 | properties：getProperties/带缺省 getProperty/setProperty 旧值/clearProperty | testProperties |
| P0 | 环境变量：getenv() 非空、缺失 null | testEnv |
| P0 | identityHashCode：稳定/默认 hashCode/null==0 | testIdentityHashCode |
| P0 | lineSeparator 与 line.separator 一致 | testLineSeparator |
| P0 | currentTimeMillis 合理范围、nanoTime 非负单调 | testTime |
| P1 | gc/runFinalization 可调用 | testGC |

## 关键规范细节（易错，已锁入断言）

- **`arraycopy` 元素类型不匹配抛 `ArrayStoreException`**；`int[] → long[]` 不拓宽（仍 ASE）。
- **`arraycopy` 支持同数组重叠**（先拷贝尾部再覆盖，右移/左移结果确定）。
- **`identityHashCode(null)==0`**；`identityHashCode(o)==o.hashCode()`（当 hash code 未被覆写）。
- **`lineSeparator()` 等于 `System.getProperty("line.separator")`**。
- **`setProperty` 首次返回 null、之后返回旧值；`clearProperty` 返回旧值**。
- **`nanoTime` 非负且单调不减**（同一次调用序列内）。

## 注意事项

- 不调用 exit/halt/System.exit；properties 测试用专属键并在末尾 clear 清理，不改全局其它键。
- headless 安全，不加 `@key`；currentTimeMillis 用宽松范围断言（避免墙钟依赖）。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）

- **arraycopy 完整矩阵**：8 种基本类型 + 引用数组在 8 组重叠窗口 `{{0,3,5},{2,0,5},{1,4,4},{3,3,3},{0,1,6},{5,0,5},{1,3,4},{2,6,4}}` 下的快照断言；同数组别名拷贝；二维数组整行拷贝；`String[]→Object[]` 协变；`srcPos>length` 但 `len==0` 合法；`MIN_VALUE/MAX_VALUE` 长度负例。
- **arraycopy 异常时序**：ASE 元素级检查——length-3 首元素不兼容立即抛出且目标 `array[0]` 保持原值；`Integer[]→String[]` 单个 null 元素穿越触发 ASE。
- **properties 活表**：`getProperties()` 返回 live table，对它的 `put` 与 `System.setProperty` 互相立即可见；24 个标准键非 null。
- **getenv**：PATH 存在；两次 `getenv()` 返回同一实例（视图身份）；`put/putAll/remove/entrySet().add` 全部 `UnsupportedOperationException`。
- **identityHashCode**：`null==0`；覆写 `hashCode()` 的类只断稳定性（不断具体值）；`identityHashCode(a) != a.hashCode()` 分歧实证。
- **SecurityManager 缺席**：`getSecurityManager()==null`（JDK-8 测试全程不安装）。
- **load/loadLibrary 负例**：精确消息锁定（见下）。

### 2. 使用方式

```java
Properties live = System.getProperties();   // live table：修改立即全局可见
System.load("/tmp/definitely-missing-lib"); // 抛 UnsatisfiedLinkError，消息含路径
```

### 3. 参考来源

- 实测 Temurin 8u372（探针目录 /tmp/lcF）：全部精确消息、重叠窗口期望值、活表语义均先实测再断言。
- Oracle Java SE 8 javadoc（System / Properties / arraycopy 类型检查规则）。
- JDK-27 侧同套探针对照（消息文本差异见下）。

### 4. 测试用例设计（映射到 SystemTest.java 新增方法）+ 易错规范细节

| 测试点 | 用例方法 |
|---|---|
| 8 原始类型 + 引用数组重叠矩阵、别名、2D 行、协变 | testArraycopyMatrix |
| IOOBE 5 形态、ASE 元素级时序、null 元素穿越 | testArraycopyExceptions |
| len==0 越界合法、MIN/MAX 长度负例 | testArraycopyEdges |
| currentTimeMillis/nanoTime 弱契约 | testTimeWeak |
| 24 标准键、活表 put/setProperty 互见（finally 清理） | testPropertiesContract |
| PATH、视图身份、4 种变更 UOE | testEnvContract |
| null==0、稳定性、内容 hash 分歧 | testIdentityHashCodeContract |
| getSecurityManager()==null | testSecurityManagerAbsent |
| load/loadLibrary 精确消息 | testLoadNegatives |
| mapLibraryName、流类型、flush | testMiscSurface |

- **易错**：JDK-8 侧 arraycopy 的 AIOOBE/ASE **消息为 null**（JDK-27 有 "arraycopy: ..." 文本，分支锁定）。
- **易错**：ASE 时序按实测断——首元素不兼容即抛，已写入元素保留。
- **易错**：`loadLibrary` 消息 8 侧精确为 `no lcFNoSuchLib in java.library.path`（27 侧追加 `: <pathlist>`，用前缀匹配）。
- **易错**：活表测试必须 `finally` 清理专属键；`setProperty` 断言首次 null、之后旧值。

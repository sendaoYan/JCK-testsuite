# ArrayStoreException — 设计方案（JDK-8）

## 1. API 功能
`java.lang.ArrayStoreException` 继承自 RuntimeException。
构造器：`ArrayStoreException()`、`ArrayStoreException(String)`（javap 双侧实测：仅这两个公共构造器，**无 cause 通道构造器**）。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/setStackTrace/fillInStackTrace/toString`。
序列化：`Serializable`，`serialVersionUID == -4522193890499838241L`（ObjectStreamClass.lookup 双侧实测一致）。

## 2. Java 用户如何使用
协变数组存入不兼容元素（aastore）或 `System.arraycopy` 分量类型不兼容时由 JVM 抛出；用户代码几乎不手工构造，主要 `catch` 诊断。

## 3. 参考来源
- Oracle javadoc：java.lang.ArrayStoreException（构造器/成员/抛出条件）。
- `javap -public`（JDK-8 temurin 8u372 与 JDK-27 对拍）确认公共构造器面。
- 本项目双侧 JDK 实测（消息文本、部分拷贝时机、initCause 行为均以实测值为准）。

## 4. 测试用例设计 + 易错规范细节
- **真触发（aastore 检查）**：`Object[] a = new String[1]; a[0] = Integer.valueOf(1)` → 本类型，消息**双侧均为 `"java.lang.Integer"`**（被存值的类名，实测直锁）；`toString` 逐字 `java.lang.ArrayStoreException: java.lang.Integer`；null 可存入（对照）。
- **arraycopy 元素级拷贝时机（实证）**：源 `{"a", Integer(2), "c"}` 拷入 `String[3]` 抛 ASE 时**目标[0] 已是 "a"、目标[1]/[2] 未动**——检查逐元素进行、失败即停，双侧一致。
- **版本差异（实测）**：arraycopy 消息 8 侧 null / 27 侧 `"arraycopy: element type mismatch: ..."`；`int[]`→`byte[]` 基本类型不兼容 8 侧 null / 27 侧 `"arraycopy: type mismatch: ..."`——`MODERN` 分支锁定。
- **对照**：`Integer` → `Comparable<?>[]` 子型兼容拷贝不抛；长度 0 拷贝不检查。

### 通用契约维度（本批统一落实）
- 构造器逐个：`getMessage`（含 `(String)null` 形态）、`getCause` 同一性、`toString` 逐字（无消息时无冒号）、`getClass()` 精确；2 构造器类型用 `getConstructor(String.class, Throwable.class)/(Throwable.class)` 抛 `NoSuchMethodException` 锁定“无 cause 构造器”这一构造器面事实。
- 层级：`instanceof` 正反向（RuntimeException/ReflectiveOperationException 族分野、非 Error、兄弟类型分离——注意 javac 拒绝不相关类型间的 `instanceof`，需先提升为 `Object`）。
- cause 生命周期：`initCause` 返回 this、一次成功；二次 → `IllegalStateException` 消息前缀 `Can't overwrite cause with ...`（含 cause 的 toString）；`initCause(this)` → `IllegalArgumentException("Self-causation not permitted")`；`initCause(null)` 成功且消耗唯一一次机会（此后再 initCause 仍 ISE）。
- suppressed：默认空；`addSuppressed` 保序；`getSuppressed` 防御拷贝；`addSuppressed(this)` → IAE `"Self-suppression not permitted"`（与 initCause 的 Self-causation 措辞区分）；`addSuppressed(null)` → NPE `"Cannot suppress a null exception."`（双侧逐字一致）。
- 栈轨迹：`setStackTrace` 四字段回读、`getStackTrace` 防御拷贝、空数组回读空、`setStackTrace(null)` → NPE（消息 27 侧为 helpful NPE，只锁类型）、`fillInStackTrace()==this`、顶层帧类名锁定。
- 打印：`printStackTrace(PrintStream→ByteArrayOutputStream)` 首行逐字、真实帧 `	at <本类>.testThrowableContract`、`Suppressed:` 小节逐字；`printStackTrace(PrintWriter→StringWriter)` 首行；`getLocalizedMessage==getMessage`；不调用无参形态（避免污染 stderr）。
- 序列化：`ObjectStreamClass.lookup` 精确 `serialVersionUID`（本批双侧实测一致直锁）+ `forClass` 同一 + 流类名。

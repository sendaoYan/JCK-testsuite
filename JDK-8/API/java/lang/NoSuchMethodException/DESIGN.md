# NoSuchMethodException — 设计方案（JDK-8）

## 1. API 功能
`java.lang.NoSuchMethodException` 继承自 ReflectiveOperationException（checked）。
构造器：`NoSuchMethodException()`、`NoSuchMethodException(String)`（javap 双侧实测：仅这两个公共构造器，**无 cause 通道构造器**）。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/setStackTrace/fillInStackTrace/toString`。
序列化：`Serializable`，`serialVersionUID == 5034388446362600923L`（ObjectStreamClass.lookup 双侧实测一致）。

## 2. Java 用户如何使用
`Class.getMethod/getConstructor/getDeclaredMethod` 等查不到成员时抛出，消息为 `类.名(参数表)` 形态。

## 3. 参考来源
- Oracle javadoc：java.lang.NoSuchMethodException（构造器/成员/抛出条件）。
- `javap -public`（JDK-8 temurin 8u372 与 JDK-27 对拍）确认公共构造器面。
- 本项目双侧 JDK 实测（消息文本、部分拷贝时机、initCause 行为均以实测值为准）。

## 4. 测试用例设计 + 易错规范细节
- **真触发**：`String.class.getMethod("nope")` → 消息逐字 `java.lang.String.nope()`（`类.名(参数表)` 形态，双侧一致）；接口无构造器 `Runnable.class.getDeclaredConstructor()` → `java.lang.Runnable.<init>()`；`getConstructor(CharSequence.class)` 消息前缀 `java.lang.String.<init>(`。
- **版本差异（实测）**：带参签名参数表 **8 侧逗号后带空格 `"charAt(int, int)"` / 27 侧无空格 `"charAt(int,int)"`**——`MODERN` 分支逐字锁定。
- **易错**：`String(StringBuilder)` 是真实公共构造器，不能拿来当"不存在"样本，选 `CharSequence`。

### 通用契约维度（本批统一落实）
- 构造器逐个：`getMessage`（含 `(String)null` 形态）、`getCause` 同一性、`toString` 逐字（无消息时无冒号）、`getClass()` 精确；2 构造器类型用 `getConstructor(String.class, Throwable.class)/(Throwable.class)` 抛 `NoSuchMethodException` 锁定“无 cause 构造器”这一构造器面事实。
- 层级：`instanceof` 正反向（RuntimeException/ReflectiveOperationException 族分野、非 Error、兄弟类型分离——注意 javac 拒绝不相关类型间的 `instanceof`，需先提升为 `Object`）。
- cause 生命周期：`initCause` 返回 this、一次成功；二次 → `IllegalStateException` 消息前缀 `Can't overwrite cause with ...`（含 cause 的 toString）；`initCause(this)` → `IllegalArgumentException("Self-causation not permitted")`；`initCause(null)` 成功且消耗唯一一次机会（此后再 initCause 仍 ISE）。
- suppressed：默认空；`addSuppressed` 保序；`getSuppressed` 防御拷贝；`addSuppressed(this)` → IAE `"Self-suppression not permitted"`（与 initCause 的 Self-causation 措辞区分）；`addSuppressed(null)` → NPE `"Cannot suppress a null exception."`（双侧逐字一致）。
- 栈轨迹：`setStackTrace` 四字段回读、`getStackTrace` 防御拷贝、空数组回读空、`setStackTrace(null)` → NPE（消息 27 侧为 helpful NPE，只锁类型）、`fillInStackTrace()==this`、顶层帧类名锁定。
- 打印：`printStackTrace(PrintStream→ByteArrayOutputStream)` 首行逐字、真实帧 `	at <本类>.testThrowableContract`、`Suppressed:` 小节逐字；`printStackTrace(PrintWriter→StringWriter)` 首行；`getLocalizedMessage==getMessage`；不调用无参形态（避免污染 stderr）。
- 序列化：`ObjectStreamClass.lookup` 精确 `serialVersionUID`（本批双侧实测一致直锁）+ `forClass` 同一 + 流类名。

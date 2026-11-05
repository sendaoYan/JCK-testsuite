# ClassNotFoundException — 设计方案（JDK-8）

## 1. API 功能
`java.lang.ClassNotFoundException` 继承自 ReflectiveOperationException（checked）。
构造器：`ClassNotFoundException()`、`(String)`、`(String, Throwable)`（无单 `(Throwable)` 形态；javap 双侧一致）。
- 特有访问器：`getException()`/`getCause()`。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/setStackTrace/fillInStackTrace/toString`。
序列化：`Serializable`，`serialVersionUID == 9176873029745254542L`（ObjectStreamClass.lookup 双侧实测一致）。

## 2. Java 用户如何使用
`Class.forName` 及类加载失败时抛出；特有 `getException()` 通道与“构造即钉死 cause、initCause 永远拒绝”是与其他异常最大的差异。

## 3. 参考来源
- Oracle javadoc：java.lang.ClassNotFoundException（构造器/成员/抛出条件）。
- `javap -public`（JDK-8 temurin 8u372 与 JDK-27 对拍）确认公共构造器面。
- 本项目双侧 JDK 实测（消息文本、部分拷贝时机、initCause 行为均以实测值为准）。

## 4. 测试用例设计 + 易错规范细节
- **特有成员 `getException()`**：`()/(String)` 形态默认 null；`(String,Throwable)` 后 `getException()==getCause()==`传入引用（同一对象）。
- **initCause 语义分叉（实测双侧一致， javadoc 既有行为）**：全部三种构造形态下 `initCause` 都抛 `IllegalStateException("Can't overwrite cause with ...")`——构造器把 Throwable 的 cause 槽位显式占位（即使传 null），与普通异常"initCause 一次可用"不同；`initCause(self)` 也是先撞 ISE 分支而非普通异常的 IAE 分支。
- **真触发（forName 生态）**：`Class.forName("no.such.Cls")` → 消息逐字 `no.such.Cls`、`getException()/getCause()` 均 null、`toString` 逐字；三参 forName 同消息；`forName("int")` → 消息 `"int"`；`forName("[Lno.such.Cls;")` → 消息**剥离数组前缀**为 `no.such.Cls`（双侧实测一致）；对照 `forName("[I")` 成功返回 `int[].class`。

### 通用契约维度（本批统一落实）
- 构造器逐个：`getMessage`（含 `(String)null` 形态）、`getCause` 同一性、`toString` 逐字（无消息时无冒号）、`getClass()` 精确；2 构造器类型用 `getConstructor(String.class, Throwable.class)/(Throwable.class)` 抛 `NoSuchMethodException` 锁定“无 cause 构造器”这一构造器面事实。
- 层级：`instanceof` 正反向（RuntimeException/ReflectiveOperationException 族分野、非 Error、兄弟类型分离——注意 javac 拒绝不相关类型间的 `instanceof`，需先提升为 `Object`）。
- cause 生命周期：`initCause` 返回 this、一次成功；二次 → `IllegalStateException` 消息前缀 `Can't overwrite cause with ...`（含 cause 的 toString）；`initCause(this)` → `IllegalArgumentException("Self-causation not permitted")`；`initCause(null)` 成功且消耗唯一一次机会（此后再 initCause 仍 ISE）。
- suppressed：默认空；`addSuppressed` 保序；`getSuppressed` 防御拷贝；`addSuppressed(this)` → IAE `"Self-suppression not permitted"`（与 initCause 的 Self-causation 措辞区分）；`addSuppressed(null)` → NPE `"Cannot suppress a null exception."`（双侧逐字一致）。
- 栈轨迹：`setStackTrace` 四字段回读、`getStackTrace` 防御拷贝、空数组回读空、`setStackTrace(null)` → NPE（消息 27 侧为 helpful NPE，只锁类型）、`fillInStackTrace()==this`、顶层帧类名锁定。
- 打印：`printStackTrace(PrintStream→ByteArrayOutputStream)` 首行逐字、真实帧 `	at <本类>.testThrowableContract`、`Suppressed:` 小节逐字；`printStackTrace(PrintWriter→StringWriter)` 首行；`getLocalizedMessage==getMessage`；不调用无参形态（避免污染 stderr）。
- 序列化：`ObjectStreamClass.lookup` 精确 `serialVersionUID`（本批双侧实测一致直锁）+ `forClass` 同一 + 流类名。

# ClassNotFoundException — 设计方案

## 1. API 功能
`java.lang.ClassNotFoundException` 继承自 `ReflectiveOperationException`，表示「ClassNotFoundException」这一错误/异常条件。
构造器：`ClassNotFoundException()`、`ClassNotFoundException(String)`、`ClassNotFoundException(String, Throwable)`。
额外访问器：`getException()`。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new ClassNotFoundException(...)` 向上层报告：
```java
try {
    Class.forName("jck.no.SuchClass");  // 触发 ClassNotFoundException
} catch (ClassNotFoundException e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- 真实触发来源：`Class.forName of missing class`（测试中可复现）。
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `ReflectiveOperationException`、父类为 `ReflectiveOperationException`、公开构造器 3 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；触发式断言 `Class.forName of missing class`。
对应 `<Simple>Test.java` 黑盒部分。



## 契约深化（2026-11）

本批（lcO2）在既有白盒+黑盒基础上追加契约级覆盖，全部断言经 JDK-8/JDK-27 双侧实测校准。

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

- serialVersionUID 精确锁定：9176873029745254542L（`ObjectStreamClass.lookup`，双侧一致直锁）。

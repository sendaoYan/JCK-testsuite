# IllegalAccessException — 设计方案

## 1. API 功能
`java.lang.IllegalAccessException` 继承自 `ReflectiveOperationException`，表示「IllegalAccessException」这一错误/异常条件。
构造器：`IllegalAccessException()`、`IllegalAccessException(String)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new IllegalAccessException(...)` 向上层报告：
```java
try {
    throw new IllegalAccessException("boom");
} catch (IllegalAccessException e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `ReflectiveOperationException`、父类为 `ReflectiveOperationException`、公开构造器 2 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；覆盖全部构造器。
对应 `<Simple>Test.java` 黑盒部分。



## 契约深化（2026-11）

本批（lcO2）在既有白盒+黑盒基础上追加契约级覆盖，全部断言经 JDK-8/JDK-27 双侧实测校准。

- **真触发（跨类私有构造器）**：`java.lang.Void` 的私有构造 `getDeclaredConstructor().newInstance()`（不 setAccessible）→ 本类型；消息含 `"java.lang.Void"`，8 侧为 `Class ... can not access a member of class java.lang.Void with modifiers "private"`，27 侧为 `class ... cannot access a member of class java.lang.Void (in module java.base) with private access`（`MODERN` 分支各锁子串）。
- **易错**：不能拿**同文件嵌套类**触发——JDK-8 同顶层类也放行、JDK-11+ nestmates 放行，双侧都不抛；必须跨类（选 Void）。
- **模块化差异（记录未断言）**：27 侧对 `java.lang.Void` 调 `setAccessible(true)` 会抛 `InaccessibleObjectException`（java.base 不 open java.lang），8 侧成功——本测试只对**自造嵌套类**做 setAccessible 成功对照，两侧一致。

### 通用契约维度（本批统一落实）
- 构造器逐个：`getMessage`（含 `(String)null` 形态）、`getCause` 同一性、`toString` 逐字（无消息时无冒号）、`getClass()` 精确；2 构造器类型用 `getConstructor(String.class, Throwable.class)/(Throwable.class)` 抛 `NoSuchMethodException` 锁定“无 cause 构造器”这一构造器面事实。
- 层级：`instanceof` 正反向（RuntimeException/ReflectiveOperationException 族分野、非 Error、兄弟类型分离——注意 javac 拒绝不相关类型间的 `instanceof`，需先提升为 `Object`）。
- cause 生命周期：`initCause` 返回 this、一次成功；二次 → `IllegalStateException` 消息前缀 `Can't overwrite cause with ...`（含 cause 的 toString）；`initCause(this)` → `IllegalArgumentException("Self-causation not permitted")`；`initCause(null)` 成功且消耗唯一一次机会（此后再 initCause 仍 ISE）。
- suppressed：默认空；`addSuppressed` 保序；`getSuppressed` 防御拷贝；`addSuppressed(this)` → IAE `"Self-suppression not permitted"`（与 initCause 的 Self-causation 措辞区分）；`addSuppressed(null)` → NPE `"Cannot suppress a null exception."`（双侧逐字一致）。
- 栈轨迹：`setStackTrace` 四字段回读、`getStackTrace` 防御拷贝、空数组回读空、`setStackTrace(null)` → NPE（消息 27 侧为 helpful NPE，只锁类型）、`fillInStackTrace()==this`、顶层帧类名锁定。
- 打印：`printStackTrace(PrintStream→ByteArrayOutputStream)` 首行逐字、真实帧 `	at <本类>.testThrowableContract`、`Suppressed:` 小节逐字；`printStackTrace(PrintWriter→StringWriter)` 首行；`getLocalizedMessage==getMessage`；不调用无参形态（避免污染 stderr）。
- 序列化：`ObjectStreamClass.lookup` 精确 `serialVersionUID`（本批双侧实测一致直锁）+ `forClass` 同一 + 流类名。

- serialVersionUID 精确锁定：6616958222490762034L（`ObjectStreamClass.lookup`，双侧一致直锁）。

# UnsupportedOperationException — 设计方案

## 1. API 功能
`java.lang.UnsupportedOperationException` 继承自 `RuntimeException`，表示「UnsupportedOperationException」这一错误/异常条件。
构造器：`UnsupportedOperationException()`、`UnsupportedOperationException(String)`、`UnsupportedOperationException(String, Throwable)`、`UnsupportedOperationException(Throwable)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new UnsupportedOperationException(...)` 向上层报告：
```java
try {
    java.util.List.of(1).add(2);  // 触发 UnsupportedOperationException
} catch (UnsupportedOperationException e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- 真实触发来源：`immutable collection modification`（测试中可复现）。
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `RuntimeException`、父类为 `RuntimeException`、公开构造器 4 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；触发式断言 `immutable collection modification`。
对应 `<Simple>Test.java` 黑盒部分。



## 契约深化（2026-11）

本批（lcO2）在既有白盒+黑盒基础上追加契约级覆盖，全部断言经 JDK-8/JDK-27 双侧实测校准。

- **真触发（可选操作契约）**：`Collections.unmodifiableList` 的 `add/remove(int)/set/iterator().remove()` 与 `unmodifiableCollection().add`、`Arrays.asList(...).remove(0)` 全部抛本类型，消息**双侧均为 null**（实测直锁）。
- **对照矩阵**：`Arrays.asList.set` 支持（不抛）、`ArrayList` 全操作自由。
- **版本差异**：JDK-9+ 的 `List.of("a")` 三个修改操作同样抛 UOE（消息 null）——仅 27 侧文件断言（8 无此 API）。

### 通用契约维度（本批统一落实）
- 构造器逐个：`getMessage`（含 `(String)null` 形态）、`getCause` 同一性、`toString` 逐字（无消息时无冒号）、`getClass()` 精确；2 构造器类型用 `getConstructor(String.class, Throwable.class)/(Throwable.class)` 抛 `NoSuchMethodException` 锁定“无 cause 构造器”这一构造器面事实。
- 层级：`instanceof` 正反向（RuntimeException/ReflectiveOperationException 族分野、非 Error、兄弟类型分离——注意 javac 拒绝不相关类型间的 `instanceof`，需先提升为 `Object`）。
- cause 生命周期：`initCause` 返回 this、一次成功；二次 → `IllegalStateException` 消息前缀 `Can't overwrite cause with ...`（含 cause 的 toString）；`initCause(this)` → `IllegalArgumentException("Self-causation not permitted")`；`initCause(null)` 成功且消耗唯一一次机会（此后再 initCause 仍 ISE）。
- suppressed：默认空；`addSuppressed` 保序；`getSuppressed` 防御拷贝；`addSuppressed(this)` → IAE `"Self-suppression not permitted"`（与 initCause 的 Self-causation 措辞区分）；`addSuppressed(null)` → NPE `"Cannot suppress a null exception."`（双侧逐字一致）。
- 栈轨迹：`setStackTrace` 四字段回读、`getStackTrace` 防御拷贝、空数组回读空、`setStackTrace(null)` → NPE（消息 27 侧为 helpful NPE，只锁类型）、`fillInStackTrace()==this`、顶层帧类名锁定。
- 打印：`printStackTrace(PrintStream→ByteArrayOutputStream)` 首行逐字、真实帧 `	at <本类>.testThrowableContract`、`Suppressed:` 小节逐字；`printStackTrace(PrintWriter→StringWriter)` 首行；`getLocalizedMessage==getMessage`；不调用无参形态（避免污染 stderr）。
- 序列化：`ObjectStreamClass.lookup` 精确 `serialVersionUID`（本批双侧实测一致直锁）+ `forClass` 同一 + 流类名。

- serialVersionUID 精确锁定：-1242599979055084673L（`ObjectStreamClass.lookup`，双侧一致直锁）。

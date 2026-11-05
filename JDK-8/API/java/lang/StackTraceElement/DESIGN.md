# java.lang.StackTraceElement 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「new 一个栈帧读字段」的冒烟。重点补：白盒结构（final、
> Serializable、仅 4 参构造器、JDK-8 无 getModuleName/getClassLoaderName/getModuleVersion）、
> toString 三种格式（`(file:line)`/`(Native Method)`/`(Unknown Source)`）、isNativeMethod 对 -2 的行号语义、
> 值语义 equals/hashCode、序列化往返保持值。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **构造器**：`StackTraceElement(String className, String methodName, String fileName, int lineNumber)`
  （JDK-8 仅此一个；7 参模块/类加载器构造器是 Java 9+）。
- **getter**：`getClassName`/`getMethodName`/`getFileName`/`getLineNumber`/`isNativeMethod`。
- **对象**：`toString`/`equals`/`hashCode`（值语义）；实现 `Serializable`。

## 2. Java 用户如何使用

- 通常由 `Throwable.getStackTrace()` 返回；也可手工构造用于 `Throwable.setStackTrace`。
- 日志框架解析 `StackTraceElement` 生成调用点；`isNativeMethod()` 判断行号为 -2 的 native 帧。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/StackTraceElement.html`）：4 参构造器、toString 格式、
  `isNativeMethod`（lineNumber == -2）。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/StackTraceElement.java`：
  final、Serializable、toString 拼装逻辑。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/StackTraceElement`。

## 4. 测试用例设计（映射到 StackTraceElementTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：final、Serializable、4 参构造器存在、7 参构造器与 getModuleName/getClassLoaderName 在 JDK-8 不存在 | testWhiteBox |
| P0 | 4 参 getter + isNativeMethod（line 42 非 native） | testGetters |
| P1 | toString 三种格式（file:line / Native Method / Unknown Source）+ line -2 → native | testToString |
| P1 | 值语义 equals/hashCode（同字段相等，行号不同不等） | testEqualsHashCode |
| P1 | 序列化往返保持 equals/hashCode | testSerialization |

## 关键规范细节（易错，已锁入断言）

- **`isNativeMethod()` 当且仅当 `lineNumber == -2`**；`toString` 对应 `C.m(Native Method)`。
- **`toString` 三种格式**：`fileName != null && lineNumber >= 0` → `C.m(F.java:42)`；
  `lineNumber == -2` → `C.m(Native Method)`；否则 → `C.m(Unknown Source)`。
- **JDK-8 无 7 参构造器，也无 `getModuleName`/`getModuleVersion`/`getClassLoaderName`**（Java 9+ 才引入）。
- **equals/hashCode 是值语义**：四个字段全相等即相等。

## 注意事项

- 序列化断言用同一字段构造，避免栈内容不确定性；headless 安全，不加 `@key`。

## 契约深化（2026-11）

### API 功能（本轮新增维度）
- fillInStackTrace 真帧闭环：`new Throwable().fillInStackTrace().getStackTrace()[0]` 的 className/methodName/lineNumber/isNativeMethod/fileName 精确断言；`toString` 形如 `Cls.method(Cls.java:N)` 且 `:N` 与 `getLineNumber()` 一致。
- 真帧类名 `Class.forName` round-trip（含静态嵌套类帧 `StackTraceElementTest$Nested`、递归帧）。
- `getStackTrace` 防御性拷贝（改返回数组元素不影响再次调用）；`setStackTrace` 拷贝数组、null → NPE、空数组合法。
- 构造器空参守卫：null className → NPE `"Declaring class is null"`、null methodName → NPE `"Method name is null"`（与 JDK-27 逐字一致）；null fileName 合法。
- 行号语义矩阵：仅 -2 为 native；-1/0/-3 非 native；toString 全矩阵（null 文件 + 任意行 → `Unknown Source`，负行号（除 -2）省略 `:N`，行 0 字面输出）。
- equals/hashCode 全契约（自反/对称/传递/null 文件参与比较/equals(null)/跨类型）。
- `ObjectStreamClass.lookup` 锁 serialVersionUID=6992337162326171013L（**JDK-8 与 JDK-27 实测一致**）；真帧序列化往返保持全部值。

### 使用方式
- 同前：单一 main 类 + `check` 断言计数。

### 参考来源
- Oracle Java SE 8 javadoc（StackTraceElement/Throwable.setStackTrace）。
- 本仓库双 JDK 实测（8u372 与 27.0.1 各跑 /tmp 探针）。

### 测试用例设计 + 易错规范细节
- 新增方法：testFillInStackTraceClosure、testCallerChainFrames、testNestedClassFrame、testGetStackTraceCopy、testSetStackTrace、testConstructorNullGuards、testLineSemanticsAndToStringMatrix、testEqualsHashCodeContract、testSerialVersionUid、testSerializationRealFrame、testPrintStackTraceFormat、testFillInStackTraceReturnsThis、testRealFrameValueEquality、testRecursionFrames（deep1/2/3 链）、testFrameTupleIndependence。
- 易错点：toString 的负行号（非 -2）**省略** `:N` 而不是字面打印（初版断言翻车后实测修正）；`setStackTrace(null)` 在 JDK-8 抛无消息 NPE、JDK-27 抛 helpful message NPE（差异见下）；同一捕获点两次 captureFrame 产生**值相等**的真帧（可作 equals 正例）。
- JDK-8 vs JDK-27 差异（27 侧文件）：JDK-27 多 7 参构造器与 getModuleName/getModuleVersion/getClassLoaderName；JDK-8 无。双侧共有行为（getter/equals/toString 矩阵/NPE 消息/serialVersionUID）逐字一致。

# UnsatisfiedLinkError — 设计方案

## 1. API 功能
`java.lang.UnsatisfiedLinkError` 继承自 `LinkageError`，表示「UnsatisfiedLinkError」这一错误/异常条件。
构造器：`UnsatisfiedLinkError()`、`UnsatisfiedLinkError(String)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new UnsatisfiedLinkError(...)` 向上层报告：
```java
try {
    throw new UnsatisfiedLinkError("boom");
} catch (UnsatisfiedLinkError e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `LinkageError`、父类为 `LinkageError`、公开构造器 2 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；覆盖全部构造器。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
构造器面（javap JDK-8 与 JDK-27 双侧实测，**完全一致**）：UnsatisfiedLinkError()、UnsatisfiedLinkError(String)。本轮逐构造器断言 getMessage/getCause（同一实例）/toString 逐字（`java.lang.UnsatisfiedLinkError: m` 与裸类名两形态），并用 `getConstructor(参数...)` + `getDeclaredConstructors().length` 把构造器签名面整个锁死。

### 2. 使用方式（本轮新增维度）
**家族归属矩阵（本批核心，一张表）**：14 个 LinkageError 家族类型逐个断言 `instanceof LinkageError/Error/Throwable/Serializable` 且**不是** `RuntimeException/Exception`（先上转 Throwable 再 instanceof，规避 javac 对不相关具体类型的转换检查）；14 条直接父类边逐一 `getSuperclass() ==` 精确锁定（LinkageError<:Error；ClassCircularityError/ClassFormatError/VerifyError/IncompatibleClassChangeError/NoClassDefFoundError/BootstrapMethodError/UnsatisfiedLinkError<:LinkageError；UnsupportedClassVersionError<:ClassFormatError；AbstractMethodError/IllegalAccessError/InstantiationError/NoSuchFieldError/NoSuchMethodError<:IncompatibleClassChangeError）；5 条兄弟互斥负例（ClassFormatError↔VerifyError、NoClassDefFoundError↔IncompatibleClassChangeError、LinkageError 不是 VirtualMachineError）。该格在 JDK-8 与 JDK-27 完全一致（javap 双侧实证）。

**Throwable 通用契约（构造后行为，全部双侧实测一致）**：
- `initCause` 一次成功返回 this；二次抛 `IllegalStateException`，消息逐字 `Can't overwrite cause with java.lang.Exception: x`（含新 cause 的 toString）；`initCause(self)` 抛 `IllegalArgumentException`，消息逐字 `Self-causation not permitted`；构造时传过 cause 形态（含显式 null）同样锁死 initCause。
- `addSuppressed(self)` 抛 `IllegalArgumentException`，消息逐字 `Self-suppression not permitted`；正常添加保序（先 s1 后 s2）；`getSuppressed()` 返回防御拷贝（改返回数组不影响二次读取）；新实例 suppressed 为空数组。
- `setStackTrace`：两帧回读逐字段相等（className/methodName/fileName/lineNumber）；`new StackTraceElement[0]` 清空；传 null 抛 NPE——**版本差异：JDK-8 消息为 null，JDK-27 为 helpful 消息（含 "stackTrace"），测试用 MODERN 分支各锁实际**。
- `printStackTrace`（到 ByteArrayOutputStream）：首行逐字 `java.lang.<Type>: m`（无消息时恰为裸类名）；自定义帧逐字打印 `at Foo.bar(Foo.java:42)` 与 `at Baz.qux(Native Method)`（lineNumber -2 → Native Method，isNativeMethod() true）；`Suppressed: java.lang.Exception: sup` 与 `Caused by: java.lang.Exception: root` 标记行存在。
- `getStackTrace()[0].getClassName()` 恰为本测试类（fillInStackTrace 捕获点）；`fillInStackTrace() == this`；`getLocalizedMessage()` 默认返回与 `getMessage()` 同一引用。

### 3. 参考来源
- Oracle javadoc java.lang.UnsatisfiedLinkError / java.lang.Throwable（initCause/addSuppressed/setStackTrace/printStackTrace 语义）与 Java Object Serialization Spec（serialVersionUID 声明语义）。
- OpenJDK 源码 java.base/share/classes/java/lang/UnsatisfiedLinkError.java（BootstrapMethodError 的 serialVersionUID=292L 即 "TODO revisit" 占位）与 java/lang/Throwable.java（"Can't overwrite cause"/"Self-causation not permitted"/"Self-suppression not permitted" 消息模板）。
- JDK-8154604（JDK-9 起 NoClassDefFoundError 附带 cause）行为差异以双侧实测为准。
- 本仓库先行探针程序（/tmp 独立编译，两侧 JDK 各跑一遍）锁定的实测值。

### 4. 测试用例设计 + 易错规范细节（本轮新增）
**真触发（招牌闭环）**：测试类声明 `static native void lcP2NativeNoLib();`、`native void lcP2NativeInstanceNoLib();`、`private static native void lcP2PrivNativeNoLib();`（不 System.load 任何库，无需 --enable-native-access——解析在链接期失败，不触碰 native 访问）直接调用：
- 全部抛 `UnsatisfiedLinkError`（instanceof LinkageError、cause null、栈非空、toString 前缀 `java.lang.UnsatisfiedLinkError: `）；同一方法连调两轮行为稳定（非一次性）。
- **消息格式版本差异（MODERN 分支逐字锁定）**：JDK-8 为 JVM 描述符形态 `UnsatisfiedLinkErrorTest.lcP2NativeNoLib()V`；JDK-27 为人类可读形态 `'void UnsatisfiedLinkErrorTest.lcP2NativeNoLib()'`（带单引号、返回类型前置）；instance/private 变体同构，消息均点名 `类.方法`。
- `System.load` 不存在路径的负例由 System 批负责，本测试只锁 native 方法解析失败路径。

**serialVersionUID 精确锁定**：`ObjectStreamClass.lookup(<Type>.class).getSerialVersionUID() == -4019343241616879428L`（JDK-8 与 JDK-27 双侧实测一致，直接锁定无需分支）。序列化往返全保持只挑 LinkageError 与 NoClassDefFoundError 两个代表做（见各 DESIGN），其余类型只锁 svuid。

**易错规范细节**：(a) 兄弟类间 `instanceof` 会被 javac 判为不可转换类型而编译失败，必须先上转 Throwable/Error；(b) 静态初始化器若以非条件 throw 结尾违反 JLS 8.7「必须能正常完成」，需 `if (true) throw` 惯用法；(c) 逃逸失败的 <clinit> 现场必须存入**外层**类静态字段，因为失败类自身的任何后续成员访问都会变成 NCDFE；(d) printStackTrace 的换行用 System.lineSeparator() 提取首行，勿硬编码 \n。

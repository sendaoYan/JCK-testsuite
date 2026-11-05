# VirtualMachineError — 设计方案

## 1. API 功能
`java.lang.VirtualMachineError` 继承自 `Error`，表示「VirtualMachineError」这一错误/异常条件。
它是 abstract 基类，具体错误由子类（如 `OutOfMemoryError`、`StackOverflowError`）表达。
构造器：`VirtualMachineError()`、`VirtualMachineError(String)`、`VirtualMachineError(String, Throwable)`、`VirtualMachineError(Throwable)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new VirtualMachineError(...)` 向上层报告：
```java
try {
    throw new VirtualMachineError("boom");
} catch (VirtualMachineError e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `Error`、父类为 `Error`、公开构造器 4 个、abstract 修饰。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；覆盖全部构造器。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- abstract 契约：`Modifier.isAbstract` 为真——直接 `new` 编译不过（测试文件可编译即编译期证据）；反射 4 个构造器逐一 `newInstance`（null 实参匹配形态）全部抛 `InstantiationException`（4/4）。
- 构造器面锁定（javap/`getDeclaredConstructors` 双测）：`()`/`(String)`/`(String,Throwable)`/`(Throwable)` 恰 4 个且全 public；abstract 不妨碍构造器存在性与子类委托。
- `serialVersionUID` 锁定：`ObjectStreamClass.lookup` = 4161983926571568670L（双侧实测一致）。
- 子类通道：匿名子类 `new VirtualMachineError(...) {}` 与具名静态嵌套子类 `SubVme` 走全部 4 个构造器——`(Throwable)` 形态 message 默认取 `cause.toString()`、`(Throwable)null` 则 message/cause 均为 null。
- 家族归属：InternalError/OutOfMemoryError/StackOverflowError/UnknownError 四个具体子类 instanceof 归属 + `getSuperclass()==VirtualMachineError` 直系断言 + 按家族句柄逐个抛/捕（同一对象）+ 家族数组多态 `getMessage` 分派。
- Throwable 通用契约在子类上：initCause 一次写/二次 `IllegalStateException`/自反 `IllegalArgumentException("Self-causation not permitted")` 精确消息、suppressed 默认空、printStackTrace 首行带具体子类名、子类经抽象父序列化往返（message/cause/suppressed 保持）。

### 2. 使用方式
家族捕获：`catch (VirtualMachineError v)` 一句兜住四种 JVM 级故障；诊断时 `v.getClass().getSimpleName()` 区分具体子类，`v.getCause()/getSuppressed()` 还原因果链。

### 3. 参考来源
Oracle javadoc（java.lang.VirtualMachineError / Throwable 序列化规范）；JLS 11.5 Error 层级；JDK-8/JDK-27 双侧 javap 与运行实测。

### 4. 测试用例设计 + 易错规范细节
- 新增 testContractStructure/testCannotInstantiate/testConstructorSurfaceViaSubclass/testConcreteFamily/testThrowableContractOnSubclass。
- 易错：abstract 类不能 `newInstance` 但 `getDeclaredConstructors` 照常返回 4 个（不能以"可反射枚举"推"可实例化"）；`(Throwable)` 构造器的 message 是 `cause.toString()` 而非 null；`initCause(this)` 抛精确消息 `Self-causation not permitted`（8/27 逐字一致）；printStackTrace 首行是**具体子类**名而非声明类型名。

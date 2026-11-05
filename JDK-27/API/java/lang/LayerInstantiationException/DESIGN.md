# LayerInstantiationException — 设计方案

## 1. API 功能
`java.lang.LayerInstantiationException` 继承自 `RuntimeException`，表示「LayerInstantiationException」这一错误/异常条件。
构造器：`LayerInstantiationException()`、`LayerInstantiationException(String)`、`LayerInstantiationException(Throwable)`、`LayerInstantiationException(String, Throwable)`。
额外访问器：无额外访问器。
继承 `Throwable` 通用契约：`getMessage/getLocalizedMessage/getCause/initCause/addSuppressed/getSuppressed/printStackTrace/getStackTrace/fillInStackTrace/toString`。

## 2. Java 用户如何使用
典型用法是 `try/catch` 捕获后诊断，或 `throw new LayerInstantiationException(...)` 向上层报告：
```java
try {
    throw new LayerInstantiationException("boom");
} catch (LayerInstantiationException e) {
    e.printStackTrace();
}
```

## 3. 开源代码实际用法调研
该类极少被用户代码主动 `new`，主要由 JDK / JVM 抛出、被用户 `catch`：
- JDK 源码 `Integer.java` 使用 `throw new NumberFormatException(...)`、`NumberFormatException.forInputString(...)` 模式（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/Integer.java`）。
- JDK 自带测试 `test/jdk/java/lang/reflect/MethodHandleAccessorsTest.java` 等直接 `new IllegalArgumentException("argument type mismatch")`、`new IllegalStateException(...)` 作为断言失败信号。
- 行为集中在构造/消息/原因/抑制异常，无额外业务语义。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName` 验证非接口、继承 `RuntimeException`、父类为 `RuntimeException`、公开构造器 4 个。
黑盒：逐构造器实例化断言 `getMessage/getCause`；`initCause` 仅一次 + 二次抛 `IllegalStateException`；`addSuppressed/getSuppressed` 往返；`printStackTrace(PrintStream)` 写入内存流；`getStackTrace` 非空；`toString` 前缀；覆盖全部构造器。
对应 `<Simple>Test.java` 黑盒部分。


## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- Throwable 机制：序列化 round-trip（message/cause 保真）、精确 toString、防御拷贝、setStackTrace、printStackTrace "Suppressed:" 节、构造面四构造器逐一验证、cause-ctor 的 message==cause.toString()。
- 真触发：同包双模块 + one-loader → "Package jck.shared in more than one module" 精确消息；同一 configuration 换 many-loaders 不再抛（每模块独立 loader 无冲突）。

### 2. 使用方式
- finderOf 自建配置，对比 one-loader 与 many-loaders 的成败差异。

### 3. 参考来源
- Oracle javadoc LayerInstantiationException；OpenJDK ModuleLayer 测试思路（自写）。

### 4. 测试用例设计 + 易错规范细节
- 断言数 17→39。
- 易错点：split package 异常只在共享 loader 时发生——many-loaders 是合法逃生门（这本身就是层的契约）。

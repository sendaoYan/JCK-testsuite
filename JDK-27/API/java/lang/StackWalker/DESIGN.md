# StackWalker — 设计方案

## 1. API 功能
`java.lang.StackWalker`（final）遍历当前线程栈帧：
- 工厂：`getInstance()`、`getInstance(Option)`、`getInstance(Set<Option>)`、`getInstance(Set, estimate)`（estimate 必须 >0，否则抛 IllegalArgumentException）
- 遍历：`walk(Function<Stream<StackFrame>,T>)`、`forEach(Consumer)`
- 调用者：`getCallerClass()`
- 选项：`StackWalker.Option`（`RETAIN_CLASS_REFERENCE`、`DROP_METHOD_INFO`、`SHOW_REFLECT_FRAMES`、`SHOW_HIDDEN_FRAMES`）

## 2. Java 用户如何使用
- 代替 `new Throwable().getStackTrace()` 获取高效、可控的栈信息。
```java
String caller = StackWalker.getInstance(RETAIN_CLASS_REFERENCE).getCallerClass().getName();
List<StackFrame> frames = StackWalker.getInstance().walk(s -> s.limit(5).toList());
```

## 3. 开源代码实际用法调研
- Log4j2/`System.Logger` 实现用 `StackWalker` 定位日志调用点。
- JDK `test/jdk/java/lang/StackWalker` 系列验证帧遍历、选项组合、`getCallerClass`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.StackWalker")` 验证 final、非接口。
黑盒：`walk` 至少一帧且能看到 `main`；`forEach` 不抛；`getCallerClass` 返回本类；四种 `getInstance` 变体非空。
对应 `<Simple>Test.java` 黑盒部分。

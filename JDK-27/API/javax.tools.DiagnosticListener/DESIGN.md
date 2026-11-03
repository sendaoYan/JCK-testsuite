# javax.tools.DiagnosticListener 测试设计

## 1. API 功能
`DiagnosticListener<S>` 是函数式接口，回调方法 `void report(Diagnostic<? extends S> diagnostic)`。编译器在编译过程中每产生一条诊断就回调一次。它作为 `JavaCompiler.getTask(...)` / `DocumentationTool.getTask(...)` 的参数传入。

## 2. Java 用户如何使用
```java
DiagnosticListener<JavaFileObject> listener = d -> System.err.println(d.getMessage(Locale.ROOT));
JavaCompiler.CompilationTask task = compiler.getTask(null, null, listener, opts, null, sources);
```
监听器可为 `null`，此时诊断按默认策略输出。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/langtools/tools/lib/snippets/SnippetUtils.java`：`parse(String body, PrintWriter pw, DiagnosticListener<JavaFileObject> dl)` 把 `dl` 传给 `compiler.getTask(pw, fm, dl, ...)`，同时把 `dl` 传给 `getStandardFileManager`。
- `~/git/jdk25u-dev/test/langtools/tools/javac/T6956638.java`：`getTask` 的 listener 参数传 `null`，并额外用 `DiagnosticCollector` 收集诊断。
- JDK 工具链 `com.sun.tools.javac.api.BasicJavacTask` 内部实现 `DiagnosticListener` 路由。

## 4. 测试用例设计 + 自动化实现
- **白盒**：`Class.forName("javax.tools.DiagnosticListener")` 断言接口、无父接口、声明 `report`。
- **黑盒**：
  1. 匿名 `DiagnosticListener` 记录回调次数，并断言每次收到 `getKind()==ERROR`。
  2. 失败编译触发回调（count>0）。
  3. 合法编译 + `null` 监听器也能成功执行。

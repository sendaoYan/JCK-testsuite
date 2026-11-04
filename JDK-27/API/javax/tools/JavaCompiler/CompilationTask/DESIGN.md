# javax.tools.JavaCompiler.CompilationTask 测试设计

## 1. API 功能
`CompilationTask` 继承 `Callable<Boolean>`，表示一次 javac 编译。方法：
- 配置：`setLocale(Locale)`、`setProcessors(Iterable<? extends Processor>)`、`addModules(Iterable<String>)`。
- 执行：`call()` 返回 `Boolean`（成功 true）。

## 2. Java 用户如何使用
```java
JavaCompiler.CompilationTask task = jc.getTask(null, null, dc, opts, null, sources);
task.setLocale(Locale.ROOT);
task.setProcessors(List.of());
task.addModules(List.of());
boolean ok = task.call();
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/tools/javac/api/JavacTaskImpl.java`：`CompilationTask` 的具体实现（`setLocale`/`setProcessors`/`addModules`/`call`）。
- `~/git/jdk25u-dev/test/langtools/tools/javac/T6956638.java`：把 `CompilationTask` 强转成 `JavacTask` 后调用 `parse/analyze/generate`。

## 4. 测试用例设计 + 自动化实现
- **白盒**：接口、继承 `Callable`、声明 `addModules`/`setProcessors`/`setLocale`/`call`。
- **黑盒**：`getTask` 返回非空；`setLocale(ROOT)`/`addModules(List.of())`/`setProcessors(List.of())` 后 `call()` 返回 true 且 `DiagnosticCollector` 为空（合法源）。

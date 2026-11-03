# javax.tools.DiagnosticCollector 测试设计

## 1. API 功能
`DiagnosticCollector<S>` 实现 `DiagnosticListener<S>`，把收到的诊断按顺序收集到内存。方法：
- 构造：无参 `DiagnosticCollector()`。
- 操作：`report(Diagnostic<? extends S>)` 追加一条诊断。
- 查询：`getDiagnostics()` 返回按报告顺序排列的 `List<Diagnostic<? extends S>>`。

## 2. Java 用户如何使用
```java
DiagnosticCollector<JavaFileObject> dc = new DiagnosticCollector<>();
JavaCompiler.CompilationTask task = compiler.getTask(null, null, dc, opts, null, sources);
boolean ok = task.call();
List<Diagnostic<? extends JavaFileObject>> diags = dc.getDiagnostics(); // 空即编译成功
```
它是“编译并收集诊断”链路里最常用的监听器实现。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/langtools/tools/javac/T6956638.java`：`DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();` 配合 `getStandardFileManager(diagnosticCollector, null, null)` 与 `getTask` 使用。
- `~/git/jdk25u-dev/test/langtools/tools/lib/snippets/SnippetUtils.java`：`DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>(); ... var diags = collector.getDiagnostics(); return diags.isEmpty();`。
- `~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/tools/javac/api/ClientCodeWrapper.java` 等工具链内部也将诊断路由到 listener。

## 4. 测试用例设计 + 自动化实现
- **白盒**：`Class.forName("javax.tools.DiagnosticCollector")` 断言 final 类、实现 `DiagnosticListener`、声明 `report`/`getDiagnostics`、单一构造器。
- **黑盒**：
  1. 新建 collector 为空（isEmpty + size==0）。
  2. 连续 report 3 条匿名诊断，断言 size==3、插入顺序保留、字段（getKind/getSource）读回正确。
  3. 作为真实编译的监听器：合法源编译成功且 `getDiagnostics()` 为空。

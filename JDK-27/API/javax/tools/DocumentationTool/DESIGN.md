# javax.tools.DocumentationTool 测试设计

## 1. API 功能
`DocumentationTool` 是 javadoc 的编程接口，继承 `Tool` 和 `OptionChecker`。核心方法：
- `getTask(Writer, JavaFileManager, DiagnosticListener, Class<?> docletClass, Iterable<String> options, Iterable<? extends JavaFileObject> compilationUnits)` 创建 `DocumentationTask`。
- `getStandardFileManager(DiagnosticListener, Locale, Charset)` 创建 `StandardJavaFileManager`。
继承方法：`name()`（返回 `"javadoc"`）、`run(...)`、`getSourceVersions()`、`isSupportedOption(String)`。

## 2. Java 用户如何使用
```java
DocumentationTool dt = ToolProvider.getSystemDocumentationTool(); // 非完整 JDK 可能为 null
if (dt != null) {
    StandardJavaFileManager fm = dt.getStandardFileManager(null, Locale.ROOT, UTF_8);
    DocumentationTool.DocumentationTask task = dt.getTask(writer, fm, diags, null, opts, sources);
    task.call();
}
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/DocumentationTool.java`：接口定义及 `Location` 枚举、`getTask`/`getStandardFileManager` 文档。
- `~/git/jdk25u-dev/src/jdk.javadoc/share/classes/jdk/javadoc/internal/api/JavadocTool.java`：具体实现 `JavacTool` 风格，`getTask` 返回 `JavadocTaskImpl`。
- JDK 测试 `test/langtools/jdk/javadoc/tool/*` 大量通过 `ToolProvider.getSystemDocumentationTool()` 或命令行运行 javadoc。

## 4. 测试用例设计 + 自动化实现
- **白盒**：接口、继承 `Tool`+`OptionChecker`、声明 `getTask`/`getStandardFileManager`。
- **黑盒**：`getSystemDocumentationTool()` 可能为 null（降级）；非 null 时断言 `name()=="javadoc"`、`getSourceVersions()` 非空且含 `RELEASE_27`、`getStandardFileManager(...)` 非空。

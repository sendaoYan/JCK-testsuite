# javax.tools.DocumentationTool.DocumentationTask 测试设计

## 1. API 功能
`DocumentationTask` 继承 `Callable<Boolean>`，表示一次 javadoc 运行。方法：
- 配置：`setLocale(Locale)`、`addModules(Iterable<String>)`。
- 执行：`call()` 返回 `Boolean`（成功 true），且继承 `Callable` 的默认 `call()` 桥接。

## 2. Java 用户如何使用
```java
DocumentationTool.DocumentationTask task = dt.getTask(writer, fm, diags, null, List.of("-d", outDir), sources);
task.setLocale(Locale.ROOT);
task.addModules(List.of());
boolean ok = task.call();
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/jdk.javadoc/share/classes/jdk/javadoc/internal/api/JavadocTaskImpl.java`：`DocumentationTask` 的具体实现，`setLocale`/`addModules`/`call` 的实现逻辑。
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/DocumentationTool.java`：`DocumentationTask` 接口定义。
- JDK 测试 `test/langtools/jdk/javadoc/doclet/...` 通过 `getTask` 驱动 javadoc。

## 4. 测试用例设计 + 自动化实现
- **白盒**：接口、继承 `Callable`、声明 `addModules`/`setLocale`/`call`。
- **黑盒**：`getSystemDocumentationTool()` 为 null 时降级；否则创建 `getTask`（含 `-d` 临时目录），`setLocale(ROOT)`、`addModules(List.of())`，`call()` 返回 true，并断言输出目录出现 `X.html`。finally 关闭文件管理器并清理临时目录。

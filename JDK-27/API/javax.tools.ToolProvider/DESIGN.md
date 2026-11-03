# javax.tools.ToolProvider 测试设计

## 1. API 功能
`ToolProvider` 提供系统工具的单例获取，全部为 static 方法：
- `getSystemJavaCompiler()` 返回系统 `JavaCompiler`，非完整 JDK 返回 null。
- `getSystemDocumentationTool()` 返回系统 `DocumentationTool`，非完整 JDK 返回 null。
- `getSystemToolClassLoader()` 返回系统工具类加载器，无则返回 null。

## 2. Java 用户如何使用
```java
JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
if (jc == null) { /* 环境不支持编译 */ }
DocumentationTool dt = ToolProvider.getSystemDocumentationTool();
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/langtools/tools/javac/T6956638.java`：`ToolProvider.getSystemJavaCompiler()`。
- `~/git/jdk25u-dev/test/langtools/tools/lib/snippets/SnippetUtils.java`：`private static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();`。
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/ToolProvider.java`：三个 static 方法定义。

## 4. 测试用例设计 + 自动化实现
- **白盒**：非接口、声明三个 static 方法、全部 static。
- **黑盒**：`getSystemJavaCompiler()` 非空且是 `Tool`/`OptionChecker`，`name()=="javac"`；`getSystemDocumentationTool()` 为空或 `Tool`；`getSystemToolClassLoader()` 返回 null 或 `ClassLoader`（实测 JDK-27 返回 null，符合“无则 null”契约）。

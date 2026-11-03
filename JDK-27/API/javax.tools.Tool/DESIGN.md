# javax.tools.Tool 测试设计

## 1. API 功能
`Tool` 是命令行工具的公共接口。方法：
- `name()`（default）返回工具名。
- `run(InputStream in, OutputStream out, OutputStream err, String... args)` 运行工具并返回退出码。
- `getSourceVersions()` 返回支持的 `SourceVersion` 集合。

## 2. Java 用户如何使用
```java
Tool javac = ToolProvider.getSystemJavaCompiler();
javac.name();                       // "javac"
javac.run(System.in, out, err, "-version");
javac.getSourceVersions();
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/tools/javac/api/JavacTool.java`：`name()` 返回 `"javac"`，`run` 实现编译命令行，`getSourceVersions` 返回支持的版本。
- `~/git/jdk25u-dev/src/jdk.javadoc/share/classes/jdk/javadoc/internal/api/JavadocTool.java`：`name()` 返回 `"javadoc"`。
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/Tool.java`：接口定义。

## 4. 测试用例设计 + 自动化实现
- **白盒**：接口、无父接口、声明 `name`/`run`/`getSourceVersions`。
- **黑盒**：javac 作为 `Tool`：`name()=="javac"`、`getSourceVersions` 含 `RELEASE_27`、`run(...,"-version")` 返回 0 且输出含 `javac`；javadoc（非 null 时）`name()=="javadoc"`、`getSourceVersions` 非空。

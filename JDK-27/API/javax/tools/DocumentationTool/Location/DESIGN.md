# javax.tools.DocumentationTool.Location 测试设计

## 1. API 功能
`DocumentationTool.Location` 是 javadoc 特定的搜索/输出位置枚举，实现 `JavaFileManager.Location`。常量：`DOCUMENTATION_OUTPUT`（输出）、`DOCLET_PATH`、`TAGLET_PATH`、`SNIPPET_PATH`。方法：`getName()`（返回枚举名）、`isOutputLocation()`（仅 `DOCUMENTATION_OUTPUT` 为 true）。

## 2. Java 用户如何使用
```java
fm.setLocation(DocumentationTool.Location.DOCUMENTATION_OUTPUT, dirs);
DocumentationTool.Location.DOCUMENTATION_OUTPUT.isOutputLocation();
```
它作为 `JavaFileManager.Location` 被文件管理器识别，是 `StandardLocation` 之外 javadoc 特有的位置。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/DocumentationTool.java`：`Location` 枚举定义与 `getName`/`isOutputLocation` 实现。
- `~/git/jdk25u-dev/src/jdk.javadoc/share/classes/jdk/javadoc/internal/tool/JavadocToolProvider.java`、`DocEnv` 等使用这些位置（`DOCLET_PATH`/`TAGLET_PATH`/`SNIPPET_PATH`）。

## 4. 测试用例设计 + 自动化实现
- **白盒**：枚举、继承 `Enum`、4 常量、实现 `JavaFileManager$Location`、声明 `getName`/`isOutputLocation`。
- **黑盒**：`values()` 顺序 DOCUMENTATION_OUTPUT/DOCLET_PATH/TAGLET_PATH/SNIPPET_PATH；`getName()` 精确匹配；`isOutputLocation()` 仅 DOCUMENTATION_OUTPUT 为 true；`valueOf` 往返。

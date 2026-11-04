# com.sun.source.doctree.LiteralTree 行为化测试设计

## 1. API 功能
- 核心方法：getBody()。

## 2. Java 用户如何使用
由 `DocTrees.instance(task).getDocTreeFactory()` 取得 `DocTreeFactory`，用 `newLiteral(...)` 工厂方法构造节点，再调用 getter 与 `accept(DocTreeScanner)`。
## 3. 开源代码实际用法调研
- JDK 编译器实现（真实来源）：`~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/source/doctree/LiteralTree.java` 中 javac 的 `com.sun.tools.javac.parser` / `com.sun.tools.javac.tree` / `com.sun.tools.javac.api` 直接构造并消费这些节点；`test/jdk` 中 `tools/javac/`、`javax/tools/` 用例覆盖编译器树/文档树 API。
- 权威用法：`com.sun.source.util.TreeScanner`/`DocTreeScanner` 是遍历这些节点的标准姿势；`Trees`/`DocTrees` 通过 `instance(JavacTask)` 取得实现。
## 4. 测试用例设计 + 自动化实现
黑盒：`DocTreeFactory` 构造 `LiteralTree` 节点，断言 `getKind()`、关键 getter 返回值与 `accept(SimpleDocTreeVisitor)` 分派。

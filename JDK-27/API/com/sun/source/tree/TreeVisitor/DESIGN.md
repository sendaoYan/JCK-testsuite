# com.sun.source.tree.TreeVisitor 行为化测试设计

## 1. API 功能
- 核心方法：visitAnnotatedType/visitBinary/…（每类节点一个 visit 方法）。

## 2. Java 用户如何使用
AST 访问者接口，每类节点一个 `visitXxx(node, p)` 方法；`SimpleTreeVisitor` 提供默认实现。
## 3. 开源代码实际用法调研
- JDK 编译器实现（真实来源）：`~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/source/tree/TreeVisitor.java` 中 javac 的 `com.sun.tools.javac.parser` / `com.sun.tools.javac.tree` / `com.sun.tools.javac.api` 直接构造并消费这些节点；`test/jdk` 中 `tools/javac/`、`javax/tools/` 用例覆盖编译器树/文档树 API。
- 权威用法：`com.sun.source.util.TreeScanner`/`DocTreeScanner` 是遍历这些节点的标准姿势；`Trees`/`DocTrees` 通过 `instance(JavacTask)` 取得实现。
## 4. 测试用例设计 + 自动化实现
黑盒：用匿名 `SimpleTreeVisitor` 覆写 `visitIdentifier`，经 `IdentifierTree.accept` 分派并断言回调被触发。

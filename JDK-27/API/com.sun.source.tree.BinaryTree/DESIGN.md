# com.sun.source.tree.BinaryTree 行为化测试设计

## 1. API 功能
- 核心方法：getLeftOperand(), getRightOperand()。

## 2. Java 用户如何使用
用 `ToolProvider.getSystemJavaCompiler().getTask(...)` 解析一段含该语法的 Java 源码 → `CompilationUnitTree`，再用 `TreeScanner` 遍历找到 `BinaryTree` 节点并调用其 getter。
## 3. 开源代码实际用法调研
- JDK 编译器实现（真实来源）：`~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/source/tree/BinaryTree.java` 中 javac 的 `com.sun.tools.javac.parser` / `com.sun.tools.javac.tree` / `com.sun.tools.javac.api` 直接构造并消费这些节点；`test/jdk` 中 `tools/javac/`、`javax/tools/` 用例覆盖编译器树/文档树 API。
- 权威用法：`com.sun.source.util.TreeScanner`/`DocTreeScanner` 是遍历这些节点的标准姿势；`Trees`/`DocTrees` 通过 `instance(JavacTask)` 取得实现。
## 4. 测试用例设计 + 自动化实现
黑盒：解析厨房水槽源码，`TreeScanner.visitBinary` 收集节点，断言 `getKind().asInterface()==BinaryTree.class`、关键 getter 非空、`accept(SimpleTreeVisitor)` 分派。

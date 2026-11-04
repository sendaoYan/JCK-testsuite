# com.sun.source.tree.LineMap 行为化测试设计

## 1. API 功能
- 核心方法：getStartPosition(), getPosition(), getLineNumber(), getColumnNumber()。

## 2. Java 用户如何使用
由 `CompilationUnitTree.getLineMap()` 取得，再按行列号查询源码偏移/行号/列号。
## 3. 开源代码实际用法调研
- JDK 编译器实现（真实来源）：`~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/source/tree/LineMap.java` 中 javac 的 `com.sun.tools.javac.parser` / `com.sun.tools.javac.tree` / `com.sun.tools.javac.api` 直接构造并消费这些节点；`test/jdk` 中 `tools/javac/`、`javax/tools/` 用例覆盖编译器树/文档树 API。
- 权威用法：`com.sun.source.util.TreeScanner`/`DocTreeScanner` 是遍历这些节点的标准姿势；`Trees`/`DocTrees` 通过 `instance(JavacTask)` 取得实现。
## 4. 测试用例设计 + 自动化实现
黑盒：解析多行源码取得 `LineMap`，断言 `getStartPosition(1)==0`、`getLineNumber(0)==1`、`getColumnNumber(0)==1`、行 2 起点大于行 1。

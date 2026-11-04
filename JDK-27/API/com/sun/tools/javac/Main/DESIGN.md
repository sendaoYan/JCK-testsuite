# com.sun.tools.javac.Main 行为化测试设计

## 1. API 功能
- 核心方法：compile(String[]), compile(String[], PrintWriter), main(String[])

## 2. Java 用户如何使用
`com.sun.tools.javac.Main.compile(String[])` 是 javac 命令行入口；`-version` 返回 0。
## 3. 开源代码实际用法调研
- JDK 编译器实现（真实来源）：`~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/tools/javac/Main.java` 中 javac 的 `com.sun.tools.javac.parser` / `com.sun.tools.javac.tree` / `com.sun.tools.javac.api` 直接构造并消费这些节点；`test/jdk` 中 `tools/javac/`、`javax/tools/` 用例覆盖编译器树/文档树 API。
- 权威用法：`com.sun.source.util.TreeScanner`/`DocTreeScanner` 是遍历这些节点的标准姿势；`Trees`/`DocTrees` 通过 `instance(JavacTask)` 取得实现。
## 4. 测试用例设计 + 自动化实现
黑盒：`compile(new String[]{"-version"})` 返回 0；`compile(String[], PrintWriter)` 返回 0 且捕获到版本输出。

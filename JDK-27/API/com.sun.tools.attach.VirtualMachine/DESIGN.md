# com.sun.tools.attach.VirtualMachine 行为化测试设计

## 1. API 功能
- 核心方法：list(), attach(), detach(), loadAgent(), getSystemProperties(), getAgentProperties()

## 2. Java 用户如何使用
`VirtualMachine.list()`/`AttachProvider.providers()` 查询本进程；VirtualMachine 的构造器/工厂/访问器见 `VirtualMachineTest.java`。
## 3. 开源代码实际用法调研
- JDK 编译器实现（真实来源）：`~/git/jdk25u-dev/src/jdk.attach/share/classes/com/sun/tools/attach/VirtualMachine.java` 中 javac 的 `com.sun.tools.javac.parser` / `com.sun.tools.javac.tree` / `com.sun.tools.javac.api` 直接构造并消费这些节点；`test/jdk` 中 `tools/javac/`、`javax/tools/` 用例覆盖编译器树/文档树 API。
- 权威用法：`com.sun.source.util.TreeScanner`/`DocTreeScanner` 是遍历这些节点的标准姿势；`Trees`/`DocTrees` 通过 `instance(JavacTask)` 取得实现。
## 4. 测试用例设计 + 自动化实现
黑盒：构造/查询 VirtualMachine 实例并断言 getter、equals/hashCode、异常层次（详见 `VirtualMachineTest.java` 的 `testBlackBox`）。

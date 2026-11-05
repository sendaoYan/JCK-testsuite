# jdk.dynalink.linker.GuardingDynamicLinkerExporter 设计案

## 1. API 功能
链接器导出抽象类 + AUTOLOAD_PERMISSION_NAME。

## 2. Java 用户如何使用
子类导出链接器。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/linker/GuardingDynamicLinkerExporter.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 abstract + 常量。
## 5. 契约深化（JDK-27）
- implements Supplier<List<GuardingDynamicLinker>>（get() 继承自 Supplier，非本类声明）。
- AUTOLOAD_PERMISSION_NAME=="dynalink.exportLinkersAutomatically"；protected 构造器；抽象类。
- 原生 JDK 运行时无已注册 exporter（ServiceLoader 空）断言。

# jdk.dynalink.StandardNamespace 设计案

## 1. API 功能
标准命名空间枚举：PROPERTY/ELEMENT/METHOD。

## 2. Java 用户如何使用
StandardNamespace.PROPERTY + findFirst。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/StandardNamespace.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒枚举常量+findFirst+valueOf。
## 5. 契约深化（JDK-27）
- values 顺序精确表 [PROPERTY, ELEMENT, METHOD]；toString==name。
- findFirst：多命名空间取声明序第一个；具名包装可解包；裸操作/null→null。
- 组合 toString（GET:PROPERTY、SET:ELEMENT:i）；每个值实现 Namespace。

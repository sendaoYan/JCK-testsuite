# jdk.dynalink.StandardNamespace 设计案

## 1. API 功能
标准命名空间枚举：PROPERTY/ELEMENT/METHOD。

## 2. Java 用户如何使用
StandardNamespace.PROPERTY + findFirst。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/StandardNamespace.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒枚举常量+findFirst+valueOf。

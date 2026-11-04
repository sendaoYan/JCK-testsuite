# jdk.dynalink.NamespaceOperation 设计案

## 1. API 功能
命名空间操作：baseOperation + 多个 namespace。

## 2. Java 用户如何使用
new NamespaceOperation(op, PROPERTY, ELEMENT) + contains/getNamespaces。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/NamespaceOperation.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒构造+命名空间计数+contains。

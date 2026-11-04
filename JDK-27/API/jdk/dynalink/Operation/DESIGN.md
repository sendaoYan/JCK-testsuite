# jdk.dynalink.Operation 设计案

## 1. API 功能
操作接口：default withNamespace/withNamespaces/named。

## 2. Java 用户如何使用
StandardOperation.GET.named("x") 等。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.dynalink/share/classes/jdk/dynalink/Operation.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。黑盒用 enum 实例测 default 方法。

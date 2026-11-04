# jdk.jshell.execution.LoaderDelegate 设计案

## 1. API 功能
类加载委托接口。

## 2. Java 用户如何使用
由执行控制实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/execution/LoaderDelegate.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface/public；黑盒用匿名实现直接调用 load/classesRedefined/addToClasspath/findClass，记录入参并断言，findClass 解析系统类。

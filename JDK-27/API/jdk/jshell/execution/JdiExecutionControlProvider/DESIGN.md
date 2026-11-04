# jdk.jshell.execution.JdiExecutionControlProvider 设计案

## 1. API 功能
JDI 执行控制提供者：name/defaultParameters/PARAM_* 常量。

## 2. Java 用户如何使用
new JdiExecutionControlProvider()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/execution/JdiExecutionControlProvider.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 name/defaultParameters/PARAM 常量。

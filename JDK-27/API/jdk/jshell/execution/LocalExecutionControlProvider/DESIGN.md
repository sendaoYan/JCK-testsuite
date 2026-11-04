# jdk.jshell.execution.LocalExecutionControlProvider 设计案

## 1. API 功能
本地执行控制提供者：name="local"。

## 2. Java 用户如何使用
new LocalExecutionControlProvider().createExecutionControl(...)。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/execution/LocalExecutionControlProvider.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 name/defaultParameters/createExecutionControl。

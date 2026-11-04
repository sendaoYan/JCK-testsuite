# jdk.jshell.execution.RemoteExecutionControl 设计案

## 1. API 功能
远程执行控制（implements ExecutionControl）：load/invoke/close。

## 2. Java 用户如何使用
new RemoteExecutionControl()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/execution/RemoteExecutionControl.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 instanceof ExecutionControl + close。

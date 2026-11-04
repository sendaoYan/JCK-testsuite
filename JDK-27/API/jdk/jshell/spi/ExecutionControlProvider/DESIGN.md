# jdk.jshell.spi.ExecutionControlProvider 设计案

## 1. API 功能
执行控制提供者接口：name/defaultParameters/createExecutionControl。

## 2. Java 用户如何使用
LocalExecutionControlProvider 等实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/spi/ExecutionControlProvider.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface/public；黑盒用 LocalExecutionControlProvider 实例以接口类型直接调用 name/defaultParameters/generate，断言 "local"/空参数表/返回 ExecutionControl。

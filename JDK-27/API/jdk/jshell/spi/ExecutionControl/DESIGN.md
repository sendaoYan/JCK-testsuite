# jdk.jshell.spi.ExecutionControl 设计案

## 1. API 功能
执行控制接口（extends AutoCloseable）：load/invoke/close。

## 2. Java 用户如何使用
LocalExecutionControl 等实现。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/spi/ExecutionControl.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 interface/public/extends AutoCloseable；黑盒用 LocalExecutionControl 实例以接口类型直接调用 load/redefine/invoke/varValue/addToClasspath/stop/extensionCommand/close，断言成功/NotImplemented/InternalException 等可观察行为；静态 generate(env,"local") 与 generate(env,"local",map) 经 ServiceLoader 返回引擎。

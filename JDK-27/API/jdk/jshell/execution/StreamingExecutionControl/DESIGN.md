# jdk.jshell.execution.StreamingExecutionControl 设计案

## 1. API 功能
流式执行控制（implements ExecutionControl，具体类）。

## 2. Java 用户如何使用
由远程执行使用。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jshell/share/classes/jdk/jshell/execution/StreamingExecutionControl.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 具体类/public/implements ExecutionControl；黑盒用只含序列化头部的空响应通道构造实例：stop/close 只写不读正常返回，load/redefine/addToClasspath/invoke/varValue 读 EOF 确定性抛 EngineTerminationException，extensionCommand 在 JDK-27 直接抛 NotImplementedException。

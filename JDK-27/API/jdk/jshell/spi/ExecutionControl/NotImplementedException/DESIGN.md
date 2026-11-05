# jdk.jshell.spi.ExecutionControl.NotImplementedException 设计案

## 1. API 功能
不支持的操作：DirectExecutionControl 的 redefine/stop/extensionCommand 三处精确消息。

## 2. Java 用户如何使用
引擎按能力抛出；线协议状态 102。

## 3. 开源代码实际用法调研
来源：DirectExecutionControl.java（JDK-27 src.zip）。

## 4. 测试用例设计 + 易错点
三条消息逐字断言（"redefine not supported"/"stop: Not supported."/"Unknown command: <cmd>"）；StreamingExecutionControl 前端 extensionCommand 另有消息 "extensionCommand not implemented."。


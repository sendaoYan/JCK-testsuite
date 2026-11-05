# jdk.jshell.spi.ExecutionControl.StoppedException 设计案

## 1. API 功能
stop() 打断运行代码的信号：唯一无参构造器，消息固定 "stopped by stop()"。

## 2. Java 用户如何使用
RemoteExecutionControl 由 JDI 注入 ThreadDeath 转换产生。

## 3. 开源代码实际用法调研
来源：ExecutionControl.java（JDK-27 src.zip）。

## 4. 测试用例设计 + 易错点
固定消息精确断言；单公共构造器；序列化保消息。


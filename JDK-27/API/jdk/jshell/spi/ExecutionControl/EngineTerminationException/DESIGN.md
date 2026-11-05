# jdk.jshell.spi.ExecutionControl.EngineTerminationException 设计案

## 1. API 功能
执行引擎终止（IO 断开/前缀非法/远端退出）的终态信号，出现后引擎不可再用。

## 2. Java 用户如何使用
StreamingExecutionControl 把一切 IOException 转成它；forwarder 非法前缀时回 RESULT_TERMINATED。

## 3. 开源代码实际用法调研
来源：ExecutionControl.java / StreamingExecutionControl.java / ExecutionControlForwarder.java。

## 4. 测试用例设计 + 易错点
构造器不设 cause；空响应流（只有序列化头）上任何命令→EngineTerminationException。


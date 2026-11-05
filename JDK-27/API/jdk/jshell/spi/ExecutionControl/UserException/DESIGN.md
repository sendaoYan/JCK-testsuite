# jdk.jshell.spi.ExecutionControl.UserException 设计案

## 1. API 功能
用户代码异常的线协议形态：causeExceptionClass() 保 FQN，构造器接收 StackTraceElement[] 并 setStackTrace。

## 2. Java 用户如何使用
DirectExecutionControl.throwConvertedInvocationException 生成；ExecutionControlForwarder 以 RESULT_USER_EXCEPTION_CHAINED 链式传输。

## 3. 开源代码实际用法调研
来源：ExecutionControl.java（JDK-27 src.zip）。

## 4. 测试用例设计 + 易错点
栈帧来自构造器参数（getStackTrace 直接返回内容）；序列化往返保消息/FQN/栈帧；engine 侧链式 cause 直至 ResolutionException 或内部异常。


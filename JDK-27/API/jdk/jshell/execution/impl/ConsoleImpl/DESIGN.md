# jdk.jshell.execution.impl.ConsoleImpl（内部类，白盒） 设计案

## 1. API 功能
jshell 远端代理的控制台桥：静态 ensureOutputAreWritten() 刷新远端控制台输出；嵌套 ConsoleProviderImpl（JdkConsoleProvider 实现，setRemoteOutput/setRemoteInput 安装远端流后 console() 产出 RemoteConsole）与 ConsoleOutputStream（控制台线协议接收端）。

## 2. Java 用户如何使用
非导出包，仅反射访问（--add-opens jdk.jshell/jdk.jshell.execution.impl=ALL-UNNAMED）。RemoteExecutionControl.invoke 的 finally 调 ensureOutputAreWritten()。

## 3. 开源代码实际用法调研
来源：jdk.jshell/src/jdk.jshell/share/classes/jdk/jshell/execution/impl/ConsoleImpl.java（JDK-27 随 JDK 源码，src.zip 提取）。

## 4. 测试用例设计 + 易错点
结构白盒（公开方法仅 ensureOutputAreWritten）+ 直接调用 ensureOutputAreWritten 无远端控制台时为 no-op + Task 枚举 8 项顺序表（WRITE_CHARS/FLUSH_OUTPUT/READ_CHARS/READ_LINE/READ_PASSWORD/FLUSH_CONSOLE/CHARSET/ENSURE_OUTPUTS_ARE_WRITTEN）。易错：嵌套类名用 $ 形式 Class.forName；写协议时 write(int) 每字节都触发一次任务分发（消息未完整时缓存不消费）。


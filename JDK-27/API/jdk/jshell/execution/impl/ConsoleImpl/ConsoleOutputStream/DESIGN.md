# jdk.jshell.execution.impl.ConsoleImpl.ConsoleOutputStream（内部类，白盒） 设计案

## 1. API 功能
OutputStream：远端写来的控制台线协议字节流在此解析为 JShellConsole 调用；每个 Task 的应答写回 sinkInput（PipeInputStream）。

## 2. Java 用户如何使用
new ConsoleOutputStream(console)；sinkInput 字段（public final InputStream）为应答通道。

## 3. 开源代码实际用法调研
来源：ConsoleImpl.java（JDK-27 src.zip）。RemoteExecutionControl 侧经 Util.forwardExecutionControlAndIO 的 consoleOutput 通道驱动。

## 4. 测试用例设计 + 易错点
反射构造 + 逐 Task 字节精确断言：WRITE_CHARS([0][len:4][chars×2]→writer().write，无应答)、FLUSH_OUTPUT(ack 0)、READ_CHARS(应答 int)、READ_LINE(prompt 应答 len+chars，null→-1)、READ_PASSWORD(同上)、FLUSH_CONSOLE(ack)、CHARSET(应答 charset 名的 chars)、ENSURE_OUTPUTS_ARE_WRITTEN(ack)。易错：int/char 均为大端；WRITE_CHARS 不应答，读它会阻塞；消息按字节流式解析，buffer[0] 决定 Task。


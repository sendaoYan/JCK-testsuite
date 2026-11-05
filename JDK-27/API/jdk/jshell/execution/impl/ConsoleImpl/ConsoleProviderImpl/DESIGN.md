# jdk.jshell.execution.impl.ConsoleImpl.ConsoleProviderImpl（内部类，白盒） 设计案

## 1. API 功能
JdkConsoleProvider 实现：静态 setRemoteOutput(InputStream)/setRemoteInput(OutputStream) 安装远端流；console(isTTY,in,out) 在两流都已安装时返回 RemoteConsole，否则 null。

## 2. Java 用户如何使用
反射实例化（public 构造器）+ 反射调用静态安装方法；运行时由 java.base 的 JdkConsole 服务发现。

## 3. 开源代码实际用法调研
来源：ConsoleImpl.java（JDK-27 src.zip）。

## 4. 测试用例设计 + 易错点
未装流时 console() 返回 null（既有断言）；装流后返回实现了 jdk.internal.io.JdkConsole 的 RemoteConsole 且 isTTY 不影响可用性。易错：静态状态跨测试残留（othervm 隔离）；RemoteConsole 的 writer()/charset() 会同步读写远端流，空流上调用会阻塞——不断言其行为。


# com.sun.jdi.connect.IllegalConnectorArgumentsException 行为化测试设计

## 1. API 功能
连接器抽象：Connector/LaunchingConnector/AttachingConnector/ListeningConnector 描述如何启动或接入目标 VM，Transport 描述传输名。

公开成员（JDK-27 javap）：
  - `public class com.sun.jdi.connect.IllegalConnectorArgumentsException extends java.lang.Exception {`
  - `public com.sun.jdi.connect.IllegalConnectorArgumentsException(java.lang.String, java.lang.String);`
  - `public com.sun.jdi.connect.IllegalConnectorArgumentsException(java.lang.String, java.util.List<java.lang.String>);`
  - `public java.util.List<java.lang.String> argumentNames();`

## 2. Java 用户如何使用
通过 VirtualMachineManager 的列表获取 connector，读 name/description/transport/defaultArguments；launch/attach/listen 用 defaultArguments 填充后调用。

## 3. 开源代码实际用法调研
来源：~/git/jdk25u-dev/test/jdk/com/sun/jdi/ 的 CompatibleConnectors.java（逐个校验 connector 与 transport）、LaunchCommandLine.java、ListenAddress.java、AcceptTimeout.java。

## 4. 测试用例设计 + 自动化实现
testBlackBox 遍历 vmm 各 connector 列表，断言 name/description/transport/defaultArguments 非空、含关键参数。

实现见同目录 `IllegalConnectorArgumentsExceptionTest.java` 的 `testWhiteBox()`（结构/继承/成员形态）与 `testBlackBox()`（组合操作行为断言）。

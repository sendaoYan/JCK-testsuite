# com.sun.jdi.Bootstrap 行为化测试设计

## 1. API 功能
JDWP 调试器的入口：Bootstrap.virtualMachineManager() 返回全局 VirtualMachineManager，枚举 launching/attaching/listening connector 并给出接口版本。

公开成员（JDK-27 javap）：
  - `public class com.sun.jdi.Bootstrap {`
  - `public com.sun.jdi.Bootstrap();`
  - `public static synchronized com.sun.jdi.VirtualMachineManager virtualMachineManager();`

## 2. Java 用户如何使用
调试器客户端首先 `Bootstrap.virtualMachineManager()`，再用 `launchingConnectors()`/`attachingConnectors()`/`listeningConnectors()` 选 connector，`defaultConnector()` 返回默认 CommandLineLaunch。

## 3. 开源代码实际用法调研
来源：~/git/jdk25u-dev/test/jdk/com/sun/jdi/ 的 CompatibleConnectors.java、RunToExit.java、ConnectedVMs.java、AcceptTimeout.java 均以 Bootstrap.virtualMachineManager() 为入口。

## 4. 测试用例设计 + 自动化实现
testBlackBox 断言 vmm 单例、各 connector 列表非空、defaultConnector 名为 com.sun.jdi.CommandLineLaunch、版本号为正。

实现见同目录 `BootstrapTest.java` 的 `testWhiteBox()`（结构/继承/成员形态）与 `testBlackBox()`（组合操作行为断言）。

# com.sun.jdi.connect.spi.ClosedConnectionException 行为化测试设计

## 1. API 功能
SPI 抽象：Connection（字节包读写）、TransportService（传输服务与 Capabilities/ListenKey），供第三方传输实现。

公开成员（JDK-27 javap）：
  - `public class com.sun.jdi.connect.spi.ClosedConnectionException extends java.io.IOException {`
  - `public com.sun.jdi.connect.spi.ClosedConnectionException();`
  - `public com.sun.jdi.connect.spi.ClosedConnectionException(java.lang.String);`

## 2. Java 用户如何使用
传输实现者继承 Connection 实现 readPacket/writePacket/close/isOpen，继承 TransportService 实现 name/capabilities/attach/startListening/accept。

## 3. 开源代码实际用法调研
来源：~/git/jdk25u-dev/src/jdk.jdi/share/classes/com/sun/jdi/connect/spi/Connection.java 与 TransportService.java 的 javadoc；test/jdk/com/sun/jdi/connect/ 下的 SPI 相关用例。

## 4. 测试用例设计 + 自动化实现
testBlackBox 用匿名子类实现抽象方法，断言 isOpen/readPacket 返回约定值、name/capabilities 可调用。

实现见同目录 `ClosedConnectionExceptionTest.java` 的 `testWhiteBox()`（结构/继承/成员形态）与 `testBlackBox()`（组合操作行为断言）。

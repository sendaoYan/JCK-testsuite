# com.sun.jdi.InvalidStackFrameException 行为化测试设计

## 1. API 功能
异常/权限类：JDI 定义的一整套已检查/运行时异常与 JDIPermission，携带调试场景的上下文（错误码、类名、参数名、进程、被抛出的对象引用）。

公开成员（JDK-27 javap）：
  - `public class com.sun.jdi.InvalidStackFrameException extends java.lang.RuntimeException {`
  - `public com.sun.jdi.InvalidStackFrameException();`
  - `public com.sun.jdi.InvalidStackFrameException(java.lang.String);`

## 2. Java 用户如何使用
Java 用户（调试器作者）在实现 JDI 客户端时，通过 `new <异常>("...")` 构造并作为方法签名里声明的 checked/unchecked 异常抛出/捕获；`getMessage()` 取诊断信息，部分子类提供专属 getter（`errorCode()`/`className()`/`argumentNames()`/`process()`/`exception()`）。

## 3. 开源代码实际用法调研
来源：JDK 源码 ~/git/jdk25u-dev/src/jdk.jdi/share/classes/<fqcn>.java（每个异常的 throw 场景）；JDK 自带测试 ~/git/jdk25u-dev/test/jdk/com/sun/jdi/ 中 ExceptionEvents.java、CatchCaughtTest.java、EventQueueDisconnectTest.java 大量触发并捕获这些异常。

## 4. 测试用例设计 + 自动化实现
testBlackBox 用多种构造器构造实例，断言 message/cause 与专属 getter 的往返一致，并用 try/throw/catch 验证其可被精确捕获。

实现见同目录 `InvalidStackFrameExceptionTest.java` 的 `testWhiteBox()`（结构/继承/成员形态）与 `testBlackBox()`（组合操作行为断言）。

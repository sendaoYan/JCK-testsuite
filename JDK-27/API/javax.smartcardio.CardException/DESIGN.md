# javax.smartcardio.CardException 设计案

## 1. API 功能
- 受检异常 `extends Exception`。
- 构造器：`CardException(String message)`、`CardException(Throwable cause)`、`CardException(String, Throwable)`。
- 继承方法：`getMessage()`、`getCause()`、`initCause()`、`printStackTrace()`。
- `Throwable cause` 构造器消息为 `cause.toString()`（`cause==null ? null : cause.toString()`）。

## 2. Java 用户如何使用
- 包装底层智能卡/PCSC 错误：`throw new CardException("transmit failed", pcscError)`。
- 调用方 `catch (CardException e) { e.getCause(); }` 获取根因。

## 3. 开源代码实际用法调研
来源：JDK 源码 `src/java.smartcardio/share/classes/javax/smartcardio/CardException.java`、`sun/security/smartcardio/PCSCException.java`。
- `PCSCException`（内部）抛出 `new CardException(message)` / `new CardException(message, cause)`（JDK 源码自用）。
- JDK 测试 `sun/security/smartcardio/*.java` 在 `catch (CardException e)` 中取 `e.getMessage()` 判错。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`CardExceptionTest.testBlackBox`）：
- 三个构造器：String 消息无 cause；Throwable 消息 == cause.toString() 且 cause 恒等；String+Throwable 两者都保留。
- 详情链：`initCause` 形成嵌套 cause。
- 可捕获性：作为受检异常在 try/catch 中捕获。

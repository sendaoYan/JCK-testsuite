# javax.smartcardio.CardNotPresentException 设计案

## 1. API 功能
- 受检异常，`extends CardException`。
- 构造器：`CardNotPresentException(String message)`、`CardNotPresentException(Throwable cause)`、`CardNotPresentException(String, Throwable)`。
- 语义：尝试连接一个没有卡的终端时抛出。

## 2. Java 用户如何使用
- `terminal.connect("T=0")` 在无卡时抛 `CardNotPresentException`，调用方单独捕获提示用户插卡。
- 因继承 `CardException`，可与其它卡错误统一 `catch (CardException e)`。

## 3. 开源代码实际用法调研
来源：JDK 源码 `sun/security/smartcardio/TerminalImpl.java`、`CardTerminal.java`。
- `TerminalImpl.connect`（JDK 源码自用）在 `SCARD_E_NO_SMARTCARD` 时抛 `new CardNotPresentException(msg)`。
- JDK 测试 `sun/security/smartcardio/TestConnect.java` 覆盖无卡时 `CardNotPresentException`。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`CardNotPresentExceptionTest.testBlackBox`）：
- 三个构造器：消息/无 cause、Throwable cause 消息 == cause.toString()、String+Throwable。
- 继承关系：`instanceof CardException`。
- 可捕获性：以父类 `CardException` 捕获。

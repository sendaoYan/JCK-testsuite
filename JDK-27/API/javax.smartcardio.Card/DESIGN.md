# javax.smartcardio.Card 设计案

## 1. API 功能
- 抽象类，`protected Card()` 构造器（供子类）。
- 抽象方法：`getATR()`、`getProtocol()`、`getBasicChannel()`、`openLogicalChannel()`、`beginExclusive()`、`endExclusive()`、`transmitControlCommand(int, byte[])`、`disconnect(boolean)`。
- 语义：一张已连接智能卡的逻辑视图；`disconnect(true)` 复位卡。

## 2. Java 用户如何使用
- 通过 `CardTerminal.connect(protocol)` 获得 Card 实例。
- 典型组合链路：`card.getBasicChannel() -> transmit(CommandAPDU) -> ResponseAPDU`；或 `card.openLogicalChannel()` 开逻辑通道。
- `beginExclusive()/endExclusive()` 包裹独占访问；`disconnect(true)` 复位并断开。

## 3. 开源代码实际用法调研
来源：JDK 源码 `sun/security/smartcardio/CardImpl.java`、`ChannelImpl.java`。
- `CardImpl` 实现所有抽象方法：`getATR()` 返回 `new ATR(...)`、`getBasicChannel()` 返回 `ChannelImpl`、`openLogicalChannel()` 通过 PCSC 开逻辑通道（JDK 源码自用）。
- `ChannelImpl` 在 `transmit` 中把 CommandAPDU 编码后发送，并把响应构造成 ResponseAPDU（JDK 源码自用）。

## 4. 测试用例设计 + 自动化实现
白盒部分（`CardTest.testWhiteBox`）：抽象类、非接口、直接继承 Object、protected 构造器、8 个抽象方法形态。
黑盒部分（`CardTest.testBlackBox`）：匿名实现组合——
- `getATR()/getProtocol()` 返回确定性值。
- `getBasicChannel().getCard() == card` 闭环；`openLogicalChannel()` 返回同一 channel。
- `transmitControlCommand` 往返；`beginExclusive/endExclusive/disconnect` 可调用。
- 通过 channel `transmit(CommandAPDU)` 得到 `ResponseAPDU` 并断言 `getSW()==0x9000`。

# javax.smartcardio.CardChannel 设计案

## 1. API 功能
- 抽象类，`protected CardChannel()` 构造器。
- 抽象方法：`getCard()`、`getChannelNumber()`、`transmit(CommandAPDU)`（返回 ResponseAPDU）、`transmit(ByteBuffer, ByteBuffer)`（返回 int）、`close()`。
- 两个 `transmit` 重载：字节数组封装版与直接 ByteBuffer 版。

## 2. Java 用户如何使用
- `card.getBasicChannel()` 获得基本逻辑通道，再 `channel.transmit(apdu)` 收发命令。
- `getChannelNumber()` 区分基本通道(0)与逻辑通道；`close()` 关闭逻辑通道。

## 3. 开源代码实际用法调研
来源：JDK 源码 `sun/security/smartcardio/ChannelImpl.java`。
- `ChannelImpl.transmit(CommandAPDU)` 编码 APDU 并返回 `ResponseAPDU`；`transmit(ByteBuffer, ByteBuffer)` 是底层零拷贝路径（JDK 源码自用）。

## 4. 测试用例设计 + 自动化实现
白盒部分（`CardChannelTest.testWhiteBox`）：抽象类、非接口、继承 Object、protected 构造器、4 个方法、2 个 transmit 重载。
黑盒部分（`CardChannelTest.testBlackBox`）：匿名实现组合——
- `getCard()` 恒等；`getChannelNumber()` 确定性。
- `transmit(CommandAPDU)` 返回 ResponseAPDU（SW=0x9000）。
- `transmit(ByteBuffer, ByteBuffer)` 消费输入并写入响应、返回字节数。
- `close()` 调用计数。

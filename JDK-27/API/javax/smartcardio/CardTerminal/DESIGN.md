# javax.smartcardio.CardTerminal 设计案

## 1. API 功能
- 抽象类，`protected CardTerminal()` 构造器。
- 抽象方法：`getName()`、`connect(String protocol)`（返回 Card）、`isCardPresent()`、`waitForCardPresent(long timeout)`、`waitForCardAbsent(long timeout)`。
- `connect("*")` 使用任意可用协议；无卡时抛 `CardNotPresentException`。

## 2. Java 用户如何使用
- 从 `TerminalFactory.terminals().list()` 拿到终端列表，`getName()` 展示名称。
- `isCardPresent()` 轮询；`waitForCardPresent(0)` 阻塞等卡插入；`connect("T=0")` 建连。

## 3. 开源代码实际用法调研
来源：JDK 源码 `sun/security/smartcardio/TerminalImpl.java`。
- `TerminalImpl.getName()` 返回 PCSC 终端名；`connect()` 依状态码抛 `CardNotPresentException`；`waitForCardPresent/Absent` 用 PCSC status（JDK 源码自用）。

## 4. 测试用例设计 + 自动化实现
白盒部分（`CardTerminalTest.testWhiteBox`）：抽象类、非接口、继承 Object、protected 构造器、5 个方法。
黑盒部分（`CardTerminalTest.testBlackBox`）：匿名实现组合——
- `getName()` 确定性；`isCardPresent()` false 且计数。
- `waitForCardPresent/Absent` 长/短超时确定性返回。
- `connect("*")`（无卡）抛 `CardNotPresentException`；`connect(null)` 返回确定性 Card 并断言协议。

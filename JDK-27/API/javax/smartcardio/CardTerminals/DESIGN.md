# javax.smartcardio.CardTerminals 设计案

## 1. API 功能
- 抽象类，`protected CardTerminals()` 构造器。
- 具体方法：`list()`（= `list(State.ALL)`）、`getTerminal(String)`（按名称查找，未找到返回 null）、`waitForChange()`（= `waitForChange(0)`）。
- 抽象方法：`list(State state)`、`waitForChange(long timeout)`。
- `list(State)` 返回不可变列表；`waitForChange` 阻塞等插入/移除。

## 2. Java 用户如何使用
- `TerminalFactory.terminals()` 得到 CardTerminals，`list()` 枚举所有终端。
- `getTerminal(name)` 取特定终端；`waitForChange()` 在循环中配合 `list(State.CARD_INSERTION)` 等待卡片事件。

## 3. 开源代码实际用法调研
来源：JDK 源码 `sun/security/smartcardio/PCSCTerminals.java`、`TerminalFactory.java`。
- `PCSCTerminals` 实现 `list(State)` 与 `waitForChange(long)`，遍历底层终端（JDK 源码自用）。
- `TerminalFactory.NoneCardTerminals` 无终端时 `list(State)` 返回空列表、`waitForChange` 抛 `IllegalStateException("no terminals")`（JDK 源码自用）。

## 4. 测试用例设计 + 自动化实现
白盒部分（`CardTerminalsTest.testWhiteBox`）：抽象类、protected 构造器、list/getTerminal/waitForChange 具体、`list(State)`/`waitForChange(long)` 抽象。
黑盒部分（`CardTerminalsTest.testBlackBox`）：匿名实现固定两个终端——
- `list()` 委托 `list(ALL)` 且包含两个终端；`list(null)` NPE。
- `getTerminal("T1")` 命中、未知返回 null、`getTerminal(null)` NPE。
- `waitForChange()` 委托 `waitForChange(0)`；`waitForChange(100)` 透传并返回 false；`waitForChange(-1)` IAE。

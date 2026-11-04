# javax.smartcardio.CardTerminals.State 设计案

## 1. API 功能
- 嵌套枚举 `javax.smartcardio.CardTerminals$State`，`enum` 常量：
  `ALL`、`CARD_PRESENT`、`CARD_ABSENT`、`CARD_INSERTION`、`CARD_REMOVAL`。
- 作为 `CardTerminals.list(State)` 的参数选择终端过滤条件。

## 2. Java 用户如何使用
- `terminals.list(State.ALL)` 取全部；`list(State.CARD_PRESENT)` 取有卡的终端。
- `list(State.CARD_INSERTION)` 与 `waitForChange()` 配合处理新插入事件。

## 3. 开源代码实际用法调研
来源：JDK 源码 `sun/security/smartcardio/PCSCTerminals.java`。
- `PCSCTerminals.list(State)` 按 `State` 常量过滤：`ALL/CARD_PRESENT/CARD_ABSENT/CARD_INSERTION/CARD_REMOVAL`（JDK 源码自用）。
- JDK 测试 `sun/security/smartcardio/TestAll.java` 使用 `State.ALL` 枚举终端。

## 4. 测试用例设计 + 自动化实现
白盒部分（`StateTest.testWhiteBox`）：`isEnum`、继承 `Enum`、5 个常量。
黑盒部分（`StateTest.testBlackBox`）：
- `values()` 长度与顺序；`name()`/`ordinal()` 精确值；`valueOf` 往返；非法名抛 IAE。

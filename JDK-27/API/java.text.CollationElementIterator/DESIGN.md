# java.text.CollationElementIterator 测试方案

## 1. API 功能（按方法分组）

- 常量：`NULLORDER`（-1，表示已到序列末尾）。
- 迭代：`next()`、`previous()`、`reset()`、`setOffset(int)`、`getOffset()`、`setText(String)`、`setText(CharacterIterator)`。
- 排序键元素拆解：静态 `primaryOrder(int)`、`secondaryOrder(int)`、`tertiaryOrder(int)`。
- 查询：`getMaxExpansion(int)`。
- 无公开构造器：实例只能通过 `RuleBasedCollator.getCollationElementIterator(...)` 获得。

## 2. Java 用户如何使用

- 构造：`RuleBasedCollator rbc = new RuleBasedCollator("< a < b < c"); CollationElementIterator it = rbc.getCollationElementIterator("abc");`
- 典型调用：`while ((e = it.next()) != CollationElementIterator.NULLORDER)` 遍历排序键元素；`it.reset()` 回到开头；`it.setOffset(0)` 定位；`it.previous()` 反向遍历。
- 拆解：`primaryOrder(e)`/`secondaryOrder(e)`/`tertiaryOrder(e)` 用于比较各强度层级。
- 组合链路：`getCollationElementIterator → next 遍历 → previous 反向遍历 → reset/setOffset → primaryOrder/secondaryOrder/tertiaryOrder`。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `src/java.base/share/classes/java/text/CollationElementIterator.java`：`previous()` 反向返回前一个元素，`NULLORDER=-1` 表示末尾。
- JDK 测试 `test/jdk/java/text/Collator/*.java` 使用 `CollationElementIterator` 遍历排序键元素并断言 `NULLORDER`。
- 通用模式：ICU/JDK 排序实现中，通过排序键元素迭代器实现多强度比较。

## 4. 测试用例设计 + 自动化实现

实现位置：`CollationElementIteratorTest.java`（`testWhiteBox()` + `testBlackBox()`）。

- 白盒：final 类、无公开构造器、`NULLORDER=-1`、迭代/拆解方法签名存在。
- 黑盒：`next()` 前进、`previous()` 返回前一元素并反向遍历、`reset/setOffset/setText` 归位、`primaryOrder/secondaryOrder/tertiaryOrder/getMaxExpansion` 非负。
- 健壮性：固定规则 `< a < b < c`、固定输入，headless-safe，不加 `@key`。

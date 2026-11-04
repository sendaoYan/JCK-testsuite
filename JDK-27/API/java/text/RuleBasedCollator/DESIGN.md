# java.text.RuleBasedCollator 测试方案

## 1. API 功能（按方法分组）

- 构造器：`RuleBasedCollator(String)`（抛 ParseException）。
- 规则：`getRules()`。
- 比较/排序键：`compare(String,String)`、`getCollationKey(String)`。
- 排序元素迭代：`getCollationElementIterator(String)`、`getCollationElementIterator(CharacterIterator)`。
- 契约：`clone`、`equals`、`hashCode`（继承 Collator 的 strength/decomposition 属性）。

## 2. Java 用户如何使用

- 构造：`new RuleBasedCollator("< a < b < c")` 定义自定义排序规则。
- 典型调用：`rbc.compare("a","b")` 按规则排序；`rbc.getRules()` 回读规则；`rbc.getCollationElementIterator("abc")` 遍历排序元素。
- 组合链路：构造规则 → `compare` 排序（含多字符串次级比较）→ `getRules` 回读 → 非法规则抛 `ParseException` → `getCollationElementIterator` 遍历 → `clone`/`equals`。

## 3. 开源代码实际用法调研（来源明确）

- JDK 源码 `java.text.Collator.getInstance(Locale)` 内部返回基于 locale 规则构建的 `RuleBasedCollator`。
- JDK 测试 `test/jdk/java/text/Collator/RuleBasedCollator/*.java`：断言自定义规则的 compare/排序元素迭代。
- 通用模式：需要非默认排序（如字典序、自定义字符优先级）时手写规则串。

## 4. 测试用例设计 + 自动化实现

实现位置：`RuleBasedCollatorTest.java`（`testWhiteBox()` 白盒 + `testRuleCompare()`/`testInvalidRules()`/`testCollationElementIterator()`/`testContract()` 黑盒）。

- 正常链路：规则 `< a < b < c` 的 compare 排序、多字符次级比较、`equals(String,String)`、`getRules` 回读。
- 异常链路：无关系字符/空规则抛 `ParseException`。
- 排序元素链路：`getCollationElementIterator` 遍历 "abc"/"ab" 得到 3/2 个元素。
- 契约：相同规则 `equals`/`hashCode` 相等、`clone` 相等、不同规则不相等。
- 健壮性：规则串固定、无 Locale/墙钟/随机/全局副作用。headless-safe，不加 `@key`。

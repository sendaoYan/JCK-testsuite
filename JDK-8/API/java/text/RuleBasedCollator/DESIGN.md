# java.text.RuleBasedCollator 测试设计案（JDK-8）

> 覆盖目标：完整契约（extends Collator、规则 compare、非法规则异常、CollationElementIterator、
> equals/clone/hashCode），非冒烟。

## 1. API 提供了哪些功能

- 构造器：`RuleBasedCollator(String rules) throws ParseException`。
- 规则：`getRules()`；`compare(String,String)`、`getCollationKey(String)`。
- 迭代：`getCollationElementIterator(String)`、`getCollationElementIterator(CharacterIterator)`。
- 契约：`clone/hashCode/equals`。

## 2. Java 用户如何使用

- `new RuleBasedCollator("< a < b < c")` 自定义排序规则；`compare` 排序、`getCollationKey` 预计算键、
  `getCollationElementIterator` 逐元素比较。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/RuleBasedCollator.java`。
- JDK-27 同目录 DESIGN.md（行为一致；JDK-8 空规则抛 `ParseException: Build rules empty.`）。

## 4. 测试用例设计（映射到 RuleBasedCollatorTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：extends Collator、ctor(String)、getRules、getCollationElementIterator | testWhiteBox |
| P0 | 规则 compare 三态 + equals(String,String) | testRuleCompare |
| P0 | 非法规则/空规则抛 ParseException | testInvalidRules |
| P0 | CollationElementIterator 元素计数（String + CharacterIterator） | testCollationElementIterator |
| P0 | equals/hashCode/clone + 异规则不等 | testContract |

## 关键规范细节（易错，已锁入断言）

- 空规则串 `""` 抛 `ParseException`（"Build rules empty."）；无关系符文本也抛 `ParseException`。

## 注意事项

- 无 Locale 依赖（规则显式给出）；无墙钟/随机；headless-safe 不加 `@key`。

## 契约深化（本批新增，两侧同构断言）

- 自定义规则序（< b < a → a/b=1；< c < a < b）；getRules 回读
- 逗号列表（同 primary+secondary，TERTIARY 下先列者在前：A,a=1）与 '=' 全等（a=A）与 '&' reset
- 非法规则矩阵（尾随关系符 message 以 "missing chars" 开头、无关系符、前导 reset、重复关系→ParseException；null→NPE）
- getCollationElementIterator(String/CharacterIterator/null NPE)

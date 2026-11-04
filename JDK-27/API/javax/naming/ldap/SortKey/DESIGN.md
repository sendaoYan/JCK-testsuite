# javax.naming.ldap.SortKey 设计案

## 1. API 功能
类。构造器：`SortKey(String attrID)`、`(String attrID, boolean ascending, String matchingRuleID)`。方法：`getAttributeID()`、`isAscending()`、`getMatchingRuleID()`。

## 2. Java 用户如何使用
典型：`new SortKey("cn")`（默认升序、无匹配规则）或 `new SortKey("cn", true, "caseIgnoreMatch")`；作为 `SortControl` 的排序键。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/SortKey.java`。

## 4. 测试用例设计
黑盒断言（SortKeyTest.testBlackBox）：三参构造字段往返；单参构造默认（升序、规则 null）；降序/null 规则。

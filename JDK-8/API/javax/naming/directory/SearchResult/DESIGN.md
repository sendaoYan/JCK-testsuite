# javax.naming.directory.SearchResult 设计案

## 1. API 功能
继承 `javax.naming.Binding`。
- 构造器：`(String name, Object obj, Attributes attrs)`、`(…, boolean isRelative)`、`(String name, String className, Object obj, Attributes attrs)`、`(…, boolean isRelative)`。
- 查询：`getAttributes()`、`getName()`/`getClassName()`/`getObject()`/`isRelative()`（继承 Binding）。
- 修改：`setAttributes(Attributes)`、`toString()`。

## 2. Java 用户如何使用
目录搜索返回 `NamingEnumeration<SearchResult>`。典型：`new SearchResult(name, obj, attrs)` → `getName()/getObject()/getAttributes()/isRelative()`。3 参构造默认 className 取对象运行时类名、isRelative=true（JDK-27 实测）。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/directory/SearchResult.java`（继承 Binding）。
- 来源：`~/git/jdk25u-dev/test/jdk` 目录搜索用例遍历 `SearchResult` 读取 name/attributes。

## 4. 测试用例设计
黑盒断言（SearchResultTest.testBlackBox）：
- 3 参构造：name/object/attributes/className(默认)/isRelative(true)。
- 5 参构造：className/relative 显式传入。
- setAttributes 往返。
- toString 含 name。

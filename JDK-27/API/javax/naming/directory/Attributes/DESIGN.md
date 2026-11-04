# javax.naming.directory.Attributes 设计案

## 1. API 功能
接口（`extends Cloneable, Serializable`）。方法：`isCaseIgnored()`、`size()`、`get(String)`、`getAll()`、`getIDs()`、`put(String,Object)`、`put(Attribute)`、`remove(String)`、`clone()`。

## 2. Java 用户如何使用
通过 `BasicAttributes` 实现。典型：`Attributes as = new BasicAttributes(false)` → `put("cn","Alice")` → `get("cn")` → `getIDs()/getAll()` → `remove("sn")`。表示一组目录属性（ID → Attribute）。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/directory/Attributes.java`。
- 来源：`~/git/jdk25u-dev/test/jdk` 中 `SearchResult`/目录操作用例以 `Attributes` 为参数与返回值。
- 来源：JDK 内部 `DirContext.getAttributes()` 返回该类型。

## 4. 测试用例设计
白盒断言（AttributesTest.testWhiteBox）：public/接口/Cloneable/Serializable + 全部方法。
黑盒断言（testBlackBox）：通过 `BasicAttributes` 验证 isCaseIgnored/size/get/put/getIDs/getAll/remove/clone 组合链路。

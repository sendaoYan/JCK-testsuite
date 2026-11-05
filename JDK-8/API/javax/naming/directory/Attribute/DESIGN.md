# javax.naming.directory.Attribute 设计案

## 1. API 功能
接口（`extends Cloneable, Serializable`）。方法：`getAll()`、`get()`、`get(int)`、`size()`、`getID()`、`contains(Object)`、`add(Object)`、`add(int,Object)`、`remove(Object)`、`remove(int)`、`set(int,Object)`、`clear()`、`isOrdered()`、`clone()`、`getAttributeSyntaxDefinition()`、`getAttributeDefinition()`。

## 2. Java 用户如何使用
用户通过 `BasicAttribute` 实现该接口，或自定义实现。典型：`Attribute a = new BasicAttribute("cn","Alice",true)` → 组合 `get/add/contains/set/remove/getAll`。
接口定义目录属性（ID + 一组值 + 有序/无序 + schema）。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/directory/Attribute.java`。
- 来源：`~/git/jdk25u-dev/test/jdk` 中 `BasicAttribute` 作为标准实现被大量使用。
- 来源：JDK 内部 `com.sun.jndi.*` 目录 Provider 实现该接口承载 LDAP 属性。

## 4. 测试用例设计
白盒断言（AttributeTest.testWhiteBox）：public/接口/Cloneable/Serializable + 全部方法存在。
黑盒断言（testBlackBox）：通过 `BasicAttribute` 实现验证接口契约（getID/isOrdered/size/get/add/set/remove/getAll/clear/clone 组合链路）。

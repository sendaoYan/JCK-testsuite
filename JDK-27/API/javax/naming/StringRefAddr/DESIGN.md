# javax.naming.StringRefAddr — 测试设计

## 1. API 功能
字符串内容地址：ctor(type, String content)；继承 RefAddr 全部行为；equals 按 type+content。

## 2. Java 用户如何使用
`new Reference(MyClass.class.getName(), new StringRefAddr("name", "jdbc/CustomerDB"))`；读取端 `((StringRefAddr) ref.get("name")).getContent()`。

## 3. 开源代码实际用法调研
- `javax.naming.LinkRef` 内部用 StringRefAddr("LinkAddress", linkName)。
- JDK test/jdk/javax/naming/RefAddr/NullContent.java 用 StringRefAddr("Type1", null)。
- Tomcat resource factory 读取 StringRefAddr 配置。

## 4. 测试用例设计（StringRefAddrTest.java）
- 白盒：public、extends RefAddr、ctor(String,String)。
- 黑盒：type/content 往返；equals 四象限（type 同/异 × content 同/异）+hashCode；null content（equals/toString/与空串区分）；放进 Reference 后按 type 取回同一实例。

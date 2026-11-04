# javax.naming.ldap.ControlFactory 设计案

## 1. API 功能
抽象类。方法：抽象 `getControlInstance(Control)`、静态 `getControlInstance(Control, Context, Hashtable<?,?>)`（根据环境属性 `java.naming.factory.control` 查找工厂；无工厂时原样返回输入控件）。

## 2. Java 用户如何使用
子类化并实现 `getControlInstance(Control)` 把通用控件转为具体控件；框架通过静态方法 + 环境查找工厂实例。典型（test/jdk `AuthzIdResponseControlFactory`）：`if (id.equals(AuthzIdResponseControl.OID)) return new AuthzIdResponseControl(...)`。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/test/jdk/javax/naming/module/src/authz/org/example/authz/AuthzIdResponseControlFactory.java`。
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/ldap/ControlFactory.java`。

## 4. 测试用例设计
白盒（ControlFactoryTest.testWhiteBox）：public/abstract + getControlInstance。
黑盒（testBlackBox）：匿名子类实现 getControlInstance；静态方法 + 空环境返回原控件。

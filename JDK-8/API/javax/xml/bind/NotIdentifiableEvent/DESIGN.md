# javax.xml.bind.NotIdentifiableEvent 契约覆盖设计（JDK-8）

## 1. API 功能
ValidationEvent 的三个标记子接口之一：XML Schema 身份约束（xs:key/xs:keyref/xs:unique）解析失败时的事件类型。**不新增任何方法或常量**——纯粹用于类型区分，接收方靠 instanceof 分派。

## 2. 使用方式
```java
if (event instanceof NotIdentifiableEvent) {
    // 身份约束问题：event.getSeverity()/getMessage()/getLocator() 全部沿用 ValidationEvent
}
```
具体构造用 helpers 包 `NotIdentifiableEventImpl(severity, message, locator[, linkedException])`。

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/NotIdentifiableEvent.html
- JSR 222 (JAXB 2.2) 规范第 6 章
- OpenJDK 8u RI 源码 `javax/xml/bind/NotIdentifiableEvent.java`
- 本仓库测试：`NotIdentifiableEventTest.java`

## 4. 测试用例设计 + 易错规范细节
- 标记形态锁定：`getDeclaredMethods().length == 0`、`getDeclaredFields().length == 0`、`getMethods().length == 4`（恰继承 4 个 ValidationEvent 方法）。
- 继承单向性：ValidationEvent.isAssignableFrom(本接口) true，反向 false。
- 常量域继承自 ValidationEvent（0/1/2 语义一致）。
- 具体类型闭环：NotIdentifiableEventImpl 3 参/4 参构造，经接口引用读 severity/message/locator/linked；instanceof ValidationEvent 成立。
- 易错点：不要给本接口断言"应该有额外常量"——规范明确无；RI 在无 schema 身份约束的普通数据上不会产生该类型事件，真实通道断言只做正构造不做过滤断言。

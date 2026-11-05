# javax.xml.bind.helpers.ValidationEventImpl 契约覆盖设计（JDK-8）

## 1. API 功能
ValidationEvent 的默认可变实现（RI 内部产生事件也用它）：3 参构造（severity,message,locator）与 4 参构造（+linkedException），全套 setter（setSeverity/setMessage/setLinkedException/setLocator）。**severity 域被校验为 [0,2]，构造与 setSeverity 双入口抛 IllegalArgumentException。**

## 2. 使用方式
```java
ValidationEventImpl e = new ValidationEventImpl(ValidationEvent.ERROR, "msg", locator, cause);
e.setSeverity(ValidationEvent.FATAL_ERROR);
handler.handleEvent(e);
e.toString(); // [severity=FATAL_ERROR,message=msg,locator=[...]]
```

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/helpers/ValidationEventImpl.html
- JSR 222 (JAXB 2.2) 规范第 6 章
- OpenJDK 8u RI 源码 `javax/xml/bind/helpers/ValidationEventImpl.java`
- 本仓库测试：`ValidationEventImplTest.java`

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵：3 参（severity/message/locator 同实例回读、linked null）、4 参（+linked 同实例）、null message/null locator 合法、2 个公共构造器。
- **severity 域校验（实测锁定）**：ctor -1/3 → IAE "Illegal severity"；setSeverity(-1)/(5) → IAE，失败后原值不变；0/1/2 全部可往返。
- 可变性矩阵：setMessage/setLocator/setLinkedException 均 set/get 往返 + null 覆写 + 替换语义。
- toString 锁定：`severity=WARNING|ERROR|FATAL_ERROR` 名称映射、含 message、locator null 时显示 `locator=null`、locator 填充后含 `line=N`。
- implements ValidationEvent。
- 易错点：severity 名称在 toString 里是 enum 风格大写（FATAL_ERROR 带下划线）。

# javax.xml.bind.helpers.NotIdentifiableEventImpl 契约覆盖设计（JDK-8）

## 1. API 功能
NotIdentifiableEvent 的默认实现：直接继承 ValidationEventImpl（构造 3 参/4 参，参数与父类一致），仅实现标记接口。全部行为（severity 校验、可变 setter、toString）继承自父类。

## 2. 使用方式
```java
NotIdentifiableEventImpl e = new NotIdentifiableEventImpl(ValidationEvent.ERROR, "msg", locator);
NotIdentifiableEvent ev = e;   // 接口分派
ValidationEvent base = e;      // 也可当基类事件用
```

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/helpers/NotIdentifiableEventImpl.html
- JSR 222 (JAXB 2.2) 规范第 6 章
- OpenJDK 8u RI 源码 `javax/xml/bind/helpers/NotIdentifiableEventImpl.java`
- 本仓库测试：`NotIdentifiableEventImplTest.java`

## 4. 测试用例设计 + 易错规范细节
- 继承链：直接父类 ValidationEventImpl、直接实现 NotIdentifiableEvent（getInterfaces 精确）、is-a ValidationEvent；2 个公共构造器。
- 构造矩阵：3 参（severity/message/locator 同实例、linked null）、4 参（+linked 同实例）。
- 交互闭环：实例喂给 DefaultValidationEventHandler——WARNING 事件 true、FATAL 事件 false（连接 helpers 两个类）。
- 继承可变性：setSeverity/setMessage/setLinkedException/setLocator 全可用；**setSeverity 越界 IAE 继承生效**；toString 父类格式（severity 名称映射）。
- 易错点：别给子类断言自有字段——一切在父类。

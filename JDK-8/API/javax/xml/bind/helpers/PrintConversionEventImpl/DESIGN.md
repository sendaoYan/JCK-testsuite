# javax.xml.bind.helpers.PrintConversionEventImpl 契约覆盖设计（JDK-8）

## 1. API 功能
PrintConversionEvent（写方向转换失败标记接口）的默认实现：直接继承 ValidationEventImpl，3 参/4 参构造与父类一致，无自有成员。与 ParseConversionEventImpl 完全同构（方向语义不同）。

## 2. 使用方式
```java
PrintConversionEventImpl e =
    new PrintConversionEventImpl(ValidationEvent.ERROR, "cannot print", locator, ise);
m.setEventHandler(h);  // h 内 instanceof PrintConversionEvent 分派
```

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/helpers/PrintConversionEventImpl.html
- JSR 222 (JAXB 2.2) 规范第 6 章
- OpenJDK 8u RI 源码 `javax/xml/bind/helpers/PrintConversionEventImpl.java`
- 本仓库测试：`PrintConversionEventImplTest.java`

## 4. 测试用例设计 + 易错规范细节
- 继承链：直接父类 ValidationEventImpl、直接实现 PrintConversionEvent、is-a ValidationEvent；2 个公共构造器。
- 构造矩阵：3 参全回读、4 参带 IllegalStateException linked。
- 交互闭环：ERROR 型实例喂 DefaultValidationEventHandler → false。
- 继承可变性：setSeverity(4) IAE 生效；setSeverity(0) 后 toString 含 severity=WARNING。
- 易错点：写方向失败需要自定义 XmlAdapter 才能真实触发（属 annotation/adapters 域），本目录按类型契约 + 真实 marshal 静默性闭环。

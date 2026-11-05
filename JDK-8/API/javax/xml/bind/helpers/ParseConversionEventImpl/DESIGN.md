# javax.xml.bind.helpers.ParseConversionEventImpl 契约覆盖设计（JDK-8）

## 1. API 功能
ParseConversionEvent（读方向转换失败标记接口）的默认实现：直接继承 ValidationEventImpl，3 参/4 参构造与父类一致，无自有成员。典型 linked exception 是 NumberFormatException 等词法转换异常。

## 2. 使用方式
```java
ParseConversionEventImpl e =
    new ParseConversionEventImpl(ValidationEvent.ERROR, "Not a number: abc", locator, nfe);
u.getEventHandler().handleEvent(e);  // 可投喂给任何 handler
```

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/helpers/ParseConversionEventImpl.html
- JSR 222 (JAXB 2.2) 规范第 6 章
- OpenJDK 8u RI 源码 `javax/xml/bind/helpers/ParseConversionEventImpl.java`
- 本仓库测试：`ParseConversionEventImplTest.java`

## 4. 测试用例设计 + 易错规范细节
- 继承链：直接父类 ValidationEventImpl、直接实现 ParseConversionEvent、is-a ValidationEvent；2 个公共构造器。
- 构造矩阵：3 参全回读（linked null）、4 参带 NumberFormatException linked。
- 交互闭环：ERROR 型实例喂 DefaultValidationEventHandler → false。
- 继承可变性：setSeverity(-3) IAE 生效；setSeverity(2) 后 toString 含 severity=FATAL_ERROR。
- 易错点：JDK-8 RI 真实通道交付的是基类 ValidationEventImpl 而非本类型（见 ParseConversionEvent/DESIGN.md 的记录性事实），本类型测试以构造+分派闭环为准。

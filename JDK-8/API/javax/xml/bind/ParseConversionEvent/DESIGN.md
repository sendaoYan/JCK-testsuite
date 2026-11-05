# javax.xml.bind.ParseConversionEvent 契约覆盖设计（JDK-8）

## 1. API 功能
ValidationEvent 的标记子接口：**读方向**（XML 词法→Java 值）类型转换失败（如 xsd:int 收到 "abc"）。不新增方法/常量，仅类型分派。

## 2. 使用方式
```java
u.setEventHandler(e -> {
    if (e instanceof ParseConversionEvent) { /* 词法转换失败 */ }
    return true;
});
```
具体构造用 helpers 包 `ParseConversionEventImpl`。

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/ParseConversionEvent.html
- JSR 222 (JAXB 2.2) 规范第 6 章
- OpenJDK 8u RI 源码 `javax/xml/bind/ParseConversionEvent.java`
- 本仓库测试：`ParseConversionEventTest.java`

## 4. 测试用例设计 + 易错规范细节
- 标记形态锁定：0 声明方法/0 常量/继承恰 4 方法；单向 isAssignableFrom。
- 具体类型闭环：ParseConversionEventImpl 3/4 参构造 + instanceof 双层（ParseConversionEvent、ValidationEvent）。
- **真实通道（重要实证）**：`<age>notanint</age>` + handler true：
  - 事件 severity==ERROR、message 含 "Not a number"（含词法原值）、**linked exception 为 NumberFormatException**、locator line==1；
  - unmarshal 不中止，int 字段回落为 0（实测锁定）；
  - **JDK-8 RI 交付的事件是基类 ValidationEventImpl，NOT instanceof ParseConversionEvent**（记录性断言，规范允许 RI 自选具体类）。
- 易错点：若断言事件 instanceof ParseConversionEvent 会在 JDK-8 RI 上必败——必须按实测记录。

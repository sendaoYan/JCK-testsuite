# javax.xml.bind.ValidationEvent 契约覆盖设计（JDK-8）

## 1. API 功能
JAXB 校验事件接口：一次数据校验/转换问题的载体。三个严重度常量 + 四个读方法（getSeverity/getMessage/getLinkedException/getLocator）。三个子接口 NotIdentifiableEvent/ParseConversionEvent/PrintConversionEvent 只是标记，不新增成员。default 事件处理器对 WARNING 继续、对 ERROR/FATAL_ERROR 中止。

## 2. 使用方式
```java
u.setEventHandler(e -> {
    if (e.getSeverity() == ValidationEvent.ERROR) log(e.getMessage());
    return true;  // true 继续，false 中止
});
// 常量：WARNING=0, ERROR=1, FATAL_ERROR=2
```
真实通道（本测试实证）：意外元素 → severity ERROR 事件（locator 定位到该元素）；malformed XML → FATAL_ERROR 事件（linked=SAXParseException）。

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/ValidationEvent.html
- JSR 222 (JAXB 2.2) 规范第 6 章（事件处理）
- OpenJDK 8u RI 源码 `javax/xml/bind/ValidationEvent.java`
- 本仓库测试：`ValidationEventTest.java`

## 4. 测试用例设计 + 易错规范细节
- 常量精确锁定：WARNING==0、ERROR==1、FATAL_ERROR==2（int、static final、boxed equals——**反射 Field.get 每次返回新 Integer，必须用 equals 不能用 ==**）。
- 接口签名：恰好 4 个声明方法、全 public abstract、返回类型逐一对号（int/String/Throwable/ValidationEventLocator）、不继承其他接口。
- 真实事件通道（文件内 @XmlRootElement Person）：
  - `<extra>` 意外元素 + handler true → 恰 1 个 ERROR 事件，message 含 "unexpected element"，locator line==1、col>=1、offset==-1、url null；
  - malformed → 恰 1 个 FATAL_ERROR 事件，linked 为 SAXParseException，且即便 handler true 也抛 UnmarshalException。
- 易错点：offset 对 SAX/Reader 源恒为 -1；FATAL 事件先送达 handler 再抛异常（断言 events.size()==1 后断言异常）。

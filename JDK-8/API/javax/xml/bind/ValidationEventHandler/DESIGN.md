# javax.xml.bind.ValidationEventHandler 契约覆盖设计（JDK-8）

## 1. API 功能
单方法函数式接口 `boolean handleEvent(ValidationEvent)`：应用程序介入 JAXB 校验/转换事件流的钩子。返回 true = 容忍继续；返回 false = 中止当前操作（Unmarshal/MarshalException）。Unmarshaller 与 Marshaller 均可安装；`setEventHandler(null)` 恢复默认处理器（ERROR/FATAL 中止）。

## 2. 使用方式
```java
class MyHandler implements ValidationEventHandler {
    List<ValidationEvent> seen = new ArrayList<>();
    boolean verdict = true;
    public boolean handleEvent(ValidationEvent e) { seen.add(e); return verdict; }
}
u.setEventHandler(new MyHandler());
```
真实通道（本测试实证）：verdict true → 意外元素被忽略、round-trip 完成；verdict false → 同一 XML 抛 UnmarshalException(cause null)；FATAL 不受 verdict 影响。

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/ValidationEventHandler.html
- JSR 222 (JAXB 2.2) 规范第 6 章
- OpenJDK 8u RI 源码 `javax/xml/bind/ValidationEventHandler.java`
- 本仓库测试：`ValidationEventHandlerTest.java`

## 4. 测试用例设计 + 易错规范细节
- 签名反射锁定：唯一方法 handleEvent(ValidationEvent)→boolean、public abstract、无父接口。
- 真实两态通道（核心）：同一 XML `<person>...<extra>x</extra></person>`：
  - verdict true：完成、字段全等、恰 1 事件（severity ERROR、locator line==1 col==47 对该固定串精确锁定）；
  - verdict false：抛 UnmarshalException 且 **cause==null**（handler 否决不是链式 cause）、事件仍恰 1 个；
  - malformed + verdict true：仍抛异常（FATAL 不可容忍）；
  - `setEventHandler(null)`：恢复默认 → 同一 XML 再次中止；
  - Marshaller 也可安装 handler 且不破坏正常 marshal。
- 易错点：col==47 只对测试内固定 XML 串成立（换串必须重探）；不要断言"handler 返回 false 时 cause 非空"。

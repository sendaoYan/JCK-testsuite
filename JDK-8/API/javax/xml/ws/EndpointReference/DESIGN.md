# EndpointReference 设计案（JDK-8 契约测试）

## 1. API 功能
javax.xml.ws.EndpointReference 是端点引用（EPR）抽象基类（JSR 224 §4.3，JAX-WS 2.1 引入）：封装寻址元数据（W3C WS-Addressing 形态由 wsaddressing.W3CEndpointReference 提供）。静态 readFrom(Source) 解析 XML；writeTo(Result) 写出；getPort(Class<T>,features) 从 EPR 直接生成代理。

## 2. 使用方式
典型路径：`EndpointReference epr = EndpointReference.readFrom(new StreamSource(...)); epr.writeTo(new StreamResult(sw)); MyPort p = epr.getPort(MyPort.class);`。本测试：结构面（readFrom 静态工厂/writeTo 抽象/getPort 泛型）+ readFrom 真实闭环（合法 EPR XML→W3CEndpointReference）+ writeTo round-trip（写出→再 readFrom）。

## 3. 参考来源
Oracle javadoc（docs.oracle.com/javase/8/docs/api/javax/xml/ws/EndpointReference.html）、JSR 224 (JAX-WS 2.2)、W3C WS-Addressing 1.0（http://www.w3.org/2005/08/addressing）、OpenJDK 8u RI、本仓库 EndpointReferenceTest。

## 4. 测试用例设计 + 易错规范细节
- 结构面：抽象类、protected 构造（无 public，不可直接实例化）；readFrom(Source) **public static** 返回 EndpointReference；writeTo(Result) 抽象 void；getPort 泛型 <T> 擦除 Object；toString 具体覆写。
- readFrom 闭环（P0）：`<EndpointReference xmlns="http://www.w3.org/2005/08/addressing"><Address>http://a</Address></EndpointReference>` → 非 null、运行时类型 **W3CEndpointReference**、toString 含 Address 内容与根元素；二次调用独立实例。
- 负例（实测）：readFrom(null) → RuntimeException（NPE 家族）；坏 XML（"not-xml"）→ WebServiceException；错根元素（<wrong/>）→ WebServiceException。
- writeTo 闭环：StreamResult 捕获 XML → 含根元素/地址/addressing ns → 输出可再 readFrom（round-trip）；writeTo(null) → RuntimeException。
- W3C 子类关系：final、extends EndpointReference、public ctor(Source)。
- 坑：EPR 的 addressing 命名空间固定 "http://www.w3.org/2005/08/addressing"；toString 输出整段 XML（非摘要）。

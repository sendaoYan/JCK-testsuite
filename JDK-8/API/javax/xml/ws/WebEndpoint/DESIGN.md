# javax.xml.ws.WebEndpoint 测试设计

## 1. API 功能

`javax.xml.ws.WebEndpoint` 标注于生成的 Service 子类的端口获取方法（`ElementType.METHOD`），单成员 `name()`（默认空串）声明该 `getXXXPort()` 方法返回的端口名（对应 WSDL `wsdl:port@name`）。它是 wsimport 生成代码形态的标准元素：`@WebServiceClient` 标注 Service 子类、`@WebEndpoint` 标注每个端口方法，运行时据此（以及方法内部传给 `getPort(portQName, sei)` 的 QName）生成代理。

## 2. 使用方式

```java
@WebServiceClient(name = "svc", targetNamespace = "urn:t", wsdlLocation = "...")
public static class EchoService extends Service {
    protected EchoService(URL u) { super(u, new QName("urn:t", "svc")); }
    @WebEndpoint(name = "EchoPort")
    public Echo getEchoPort() { return super.getPort(new QName("urn:t", "EchoPort"), Echo.class); }
}
```
本测试做两级闭环：①反射级——端口方法 / 裸标注方法 / 未标注方法的注解回读与默认值；②运行时级——写最小 document/literal wrapped WSDL 到临时文件 → 实例化该 Service 子类 → `getServiceName()/getPorts()` 回读 → 调用 `@WebEndpoint` 标注方法得到 SEI 代理（`proxy instanceof Echo`、`instanceof BindingProvider`；不发起网络调用）。

## 3. 参考来源

- Oracle javadoc（Java SE 8）: docs.oracle.com/javase/8/docs/api/javax/xml/ws/WebEndpoint.html
- JSR 224 (JAX-WS 2.2) §5.2（Service 与生成代码）
- OpenJDK 8u RI（`com.sun.xml.internal.ws.client.WSServiceDelegate` 代理生成）
- 本仓库测试：`JDK-8/API/javax/xml/ws/WebEndpoint/WebEndpointTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：
- 结构/元注解：单成员 name（String，默认 `""`）；Target 恰 {METHOD}；其余同族矩阵。
- 回读：name="EchoPort" 精确回读；裸标注 name 实例回读空串；未标注方法 null；端口方法上声明注解恰 1 个（仅 WebEndpoint）。
- 生成服务闭环：临时 WSDL（Files.createTempFile，finally 删除）+ toURI().toURL()；getServiceName == {urn:t}svc；getPorts 含 {urn:t}EchoPort；调端口方法得代理 instanceof SEI 与 BindingProvider。

易错规范细节：
1. `Path` 转 URL 是 `toUri().toURL()`（**大写 U 的 toUri**），`getPath()` 在非 ASCII 路径会踩坑。
2. Service 子类构造器 `super(URL, QName)`：**URL 传 null 时运行时回落读取类上 @WebServiceClient.wsdlLocation**（见 WebServiceClient 目录）；闭环测试必须传显式临时文件 URL。
3. 端口方法的注解 name 必须与 WSDL 端口名一致才能取到代理——本测试为生成形态自洽（同名 EchoPort）；name 与 WSDL 不一致时 getPort 抛 WebServiceException，属负例可选。
4. `proxy instanceof Echo` 的前提是 SEI 标注了 `@WebService(name="EchoPort", targetNamespace="urn:t")`（javax.jws），name/namespace 与 WSDL portType 对齐，否则代理生成报错。
5. 生成代理不发网络调用，可安全在无网环境断言。

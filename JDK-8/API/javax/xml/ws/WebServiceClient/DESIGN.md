# javax.xml.ws.WebServiceClient 测试设计

## 1. API 功能

`javax.xml.ws.WebServiceClient` 标注于 wsimport 生成的 Service 子类（`ElementType.TYPE`），三成员：`name`（WSDL service 名）、`targetNamespace`、`wsdlLocation`（生成时所用 WSDL 的位置）。它使生成的 Service 子类携带自描述元数据——当子类构造器以 `super(null, qname)` 调用时，运行时会**回落读取本注解的 wsdlLocation** 去 定位 WSDL。

## 2. 使用方式

```java
@WebServiceClient(name = "svc", targetNamespace = "urn:t", wsdlLocation = "http://x/svc?wsdl")
public static class EchoService extends Service {
    protected EchoService(URL explicit) { super(explicit, new QName("urn:t", "svc")); }
    protected EchoService() { super(null, new QName("urn:t", "svc")); }  // 回落读注解 wsdlLocation
}
```
本测试三通道：①注解矩阵（结构/元注解/三成员默认值全空串——javadoc 未写必填，实测均带默认）；②全成员/裸标注回读；③运行时通道——`new EchoService()`（null URL）触发注解 wsdlLocation 加载失败 → WebServiceException 且消息含该 URL（证明注解值真实流入运行时）；显式临时 WSDL URL 时注解位置被覆盖、服务正常构建并可生成 SEI 代理。

## 3. 参考来源

- Oracle javadoc（Java SE 8）: docs.oracle.com/javase/8/docs/api/javax/xml/ws/WebServiceClient.html
- JSR 224 (JAX-WS 2.2) §5.2、§7.7（生成 Service 类的注解要求）
- OpenJDK 8u RI（`com.sun.xml.internal.ws.client.WSServiceDelegate.parseWDDL` 回落逻辑）
- 本仓库测试：`JDK-8/API/javax/xml/ws/WebServiceClient/WebServiceClientTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：
- 结构/元注解：成员集合恰 `{name, targetNamespace, wsdlLocation}`；Target 恰 {TYPE}；其余同族矩阵。
- 默认值：三成员均 `""`（实测 getDefaultValue 非全 null——与"必填"直觉相反）。
- 回读：三元组精确、裸标注全空串、equals/hashCode/toString、未标注类 null。
- 运行时通道（三级）：null URL + 坏注解 wsdlLocation → WebServiceException（消息含注解 URL）；显式临时 WSDL URL → getServiceName/getWSDLDocumentLocation/代理生成成功；裸注解 + null URL → 空服务（getWSDLDocumentLocation null、getPorts 空、不抛）。

易错规范细节：
1. **`super(null, qn)` 的回落语义**是本注解最重要的运行时行为：null URL 不是"无 WSDL 服务"，而是"用注解里的 wsdlLocation"——裸注解（空串）时才退化为无 WSDL 服务且不抛异常。
2. 坏 wsdlLocation 失败抛 WebServiceException（RI: Failed to access the WSDL...）；消息含注解 URL 文本，可作包含性断言；DNS 解析失败在无网环境确定触发。
3. 临时 WSDL 用 `Files.createTempFile` + finally 删除；断言 `getWSDLDocumentLocation().sameFile(url)` 用 sameFile 而非 equals（等价 URL 归一化）。
4. name/targetNamespace 必须与显式传入的 QName 一致才稳定（RI 以 WSDL service 名为准），测试中两者对齐为 {urn:t}svc。
5. Service 子类的 protected 构造器是唯一扩展点（Service 无 public 构造器）；子类构造器内不可读取注解值（编译期常量已在类上）。

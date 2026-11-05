# javax.xml.ws.WebFault 测试设计

## 1. API 功能

`javax.xml.ws.WebFault` 标注于从 WSDL fault 消息生成的异常类（`ElementType.TYPE`），四成员：`name`（fault 元素本地名）、`targetNamespace`、`faultBean`（承载 fault 内容的 bean 类全限定名）、`messageName`（JAX-WS 2.2 新增，故障消息名，用于与 fault 元素名区分）。生成的异常类携带 `getFaultInfo()` 方法返回 bean，服务端与客户端借助它往返故障数据。

## 2. 使用方式

```java
@WebFault(name = "echoFault", targetNamespace = "urn:t",
          faultBean = "EchoFaultBean", messageName = "echoFaultMsg")
public static class EchoFaultException extends Exception {
    private final EchoFaultBean faultInfo;
    public EchoFaultException(String message, EchoFaultBean faultInfo) { super(message); this.faultInfo = faultInfo; }
    public EchoFaultBean getFaultInfo() { return faultInfo; }
}
```
本测试：①注解矩阵（结构/元注解/四成员默认值全空串）；②全成员与裸标注异常类回读；③生成形态结构契约（异常 message 与 getFaultInfo 同实例往返）；④内建回归——javax.xml.ws 自身异常族（ProtocolException/WebServiceException/SOAPFaultException/HTTPException）均**不带**该注解。

## 3. 参考来源

- Oracle javadoc（Java SE 8）: docs.oracle.com/javase/8/docs/api/javax/xml/ws/WebFault.html
- JSR 224 (JAX-WS 2.2) §3.5（Service-Specific Exception 与 fault bean）
- OpenJDK 8u RI
- 本仓库测试：`JDK-8/API/javax/xml/ws/WebFault/WebFaultTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：
- 结构/元注解：成员集合恰 4 个 `{name, targetNamespace, faultBean, messageName}` 全 String 全默认空串；Target 恰 {TYPE}；其余同族矩阵。
- 回读：四元组精确、裸标注全空串、equals/hashCode/toString、未标注异常类 null。
- 生成形态：构造 (message, faultInfo) → getMessage/getFaultInfo 同实例回读、getFaultInfo 签名返回 bean 类型。
- 内建回归：四个内建异常类均无 @WebFault。

易错规范细节：
1. **messageName 是 JAX-WS 2.2 新增成员**（早期 2.1 资料只有三个），declaredMethods.length 必须断 4。
2. faultBean 是 **String 类名**，不是 Class 对象；且它不要求该类真实存在（工具链生成期才解析），测试可不定义同名类，回读照常精确。
3. 规范要求故障异常构造器形态 `(String message, FaultBean faultInfo)` 且提供 `getFaultInfo()`——这是生成代码约定，运行时不强制；测试在自己定义的异常上按该形态自证。
4. javax.xml.ws 内建异常族不使用 @WebFault（该注解只用于从 WSDL 生成的故障类）——作为回归断言防实现漂移。
5. wsimport 生成的异常还带 `(String, FaultBean, Throwable)` 三参构造器——属惯例，不锁。

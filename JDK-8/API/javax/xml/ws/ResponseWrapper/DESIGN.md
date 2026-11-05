# javax.xml.ws.ResponseWrapper 测试设计

## 1. API 功能

`javax.xml.ws.ResponseWrapper` 标注于 SEI 方法（`ElementType.METHOD`），为 document/literal wrapped 风格指定**响应**包装 bean 元数据：`localName` / `targetNamespace` / `className` / `partName`。与 `RequestWrapper` 对偶（请求/响应各自独立 bean），常同时标注于同一方法。wsimport 生成的 SEI 方法与 wsgen 输出 WSDL 时消费。

## 2. 使用方式

```java
@WebService(name = "EchoPort", targetNamespace = "urn:t")
interface EchoSei {
    @RequestWrapper(localName = "echo", targetNamespace = "urn:t", className = "EchoReq")
    @ResponseWrapper(localName = "echoResponse", targetNamespace = "urn:t",
                     className = "com.example.EchoResponseBean", partName = "parameters")
    String echo(String s);
}
```
本测试覆盖：全成员回读、与 RequestWrapper 同方法共存（两者 localName 不同、namespace 可相同）、裸标注默认值矩阵、未标注负例。

## 3. 参考来源

- Oracle javadoc（Java SE 8）: docs.oracle.com/javase/8/docs/api/javax/xml/ws/ResponseWrapper.html
- JSR 224 (JAX-WS 2.2) §7.9
- OpenJDK 8u RI（JAX-WS 2.2 API 注解源码）
- 本仓库测试：`JDK-8/API/javax/xml/ws/ResponseWrapper/ResponseWrapperTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：
- 结构/元注解：成员集合恰 4 个 `{localName, targetNamespace, className, partName}` 全 String 全带默认；Target 恰 {METHOD}；其余同注解族通用矩阵。
- 默认值：逐成员空串。
- 回读：四元组精确、与 RequestWrapper 共存互不干扰（各回读各值）、裸标注实例默认、equals/hashCode/toString、负例（未标注方法 / 类级）。

易错规范细节：
1. **partName 成员存在**（与 RequestWrapper 同坑，勿漏）。
2. 请求/响应包装 localName 必须不同才合法映射，但注解本身不校验——断言"两者不同"只能针对自造辅助标注的回读值，不能当作运行时约束。
3. className 是 String（bean 的全限定名），不是 Class 对象——与 `@WebServiceRef.type`（Class）区分。
4. 默认空串语义：工具链在成员为空时按默认映射规则推导（如方法名+"Response"）；该推导属 wsimport 行为，运行时断言不了。

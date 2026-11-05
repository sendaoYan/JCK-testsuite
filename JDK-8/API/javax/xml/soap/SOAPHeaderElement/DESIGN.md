# javax.xml.soap.SOAPHeaderElement 契约测试设计

## 1. API 功能

`SOAPHeaderElement`（`extends SOAPElement`，8 个自有方法）表示 SOAP Header 的直接子元素（header block），承载 SOAP 中继路由语义：1.1 的 `actor` 与 1.2 的 `role`（同一概念的两种拼写）、`mustUnderstand`、1.2 的 `relay`。它是 SOAP 头处理模型（examineHeaderElements/extractHeaderElements 过滤依据）的核心值类型。

## 2. 使用方式

主通道：`SOAPHeader h = msg.getSOAPHeader(); SOAPHeaderElement he = h.addHeaderElement(new QName("urn:h","hdr","h"))`，随后 `setActor/setRole/setMustUnderstand/setRelay` + 回读；经 `h.examineAllHeaderElements()` 视角迭代。1.1 消息用 `MessageFactory.newInstance()`，1.2 用 `newInstance(SOAPConstants.SOAP_1_2_PROTOCOL)`。

## 3. 参考来源

- Oracle javadoc Java SE 8：docs.oracle.com/javase/8/docs/api/javax/xml/soap/SOAPHeaderElement.html
- SOAP 1.1 / SOAP 1.2（RFC 4227 之外的中继模型）、JSR 67 (SAAJ 1.3)
- OpenJDK 8u RI：`com.sun.xml.internal.messaging.saaj.soap.ver1_1.HeaderElement1_1Impl`、`ver1_2.HeaderElement1_2Impl`
- 本仓库测试：`JDK-8/API/javax/xml/soap/SOAPHeaderElement/SOAPHeaderElementTest.java`

## 4. 测试用例设计 + 易错规范细节

维度：反射方法面（恰好 8 方法）→ 1.1 真实通道（actor 回读/actor DOM 属性/文本/编码风格/角色常量）→ 1.1 上 1.2 专属 API 的 UOE 矩阵 → MU 字面量（1.1 "1"/"0"）→ 1.2 全语义（role/relay/MU/actor）→ 1.2 setActor-setRole 同存储 → 父链与 detach → examineAllHeaderElements 与 setValue。

易错点（实测锁定）：

1. **SOAP 1.1 上 getRole/setRole/getRelay/setRelay 全部抛 `UnsupportedOperationException`**，消息精确为 "Role not supported by SOAP 1.1" / "Relay not supported by SOAP 1.1"；调用同时 RI 打 SEVERE 日志（正常，不断言日志）。
2. **mustUnderstand 属性字面量分版本**：1.1 写 `"1"/"0"`，1.2 写 `"true"/"false"`；经 `getAttributeValue(QName(env, "mustUnderstand"))` 可双确认。
3. **1.2 上 setActor 与 setRole 是同一存储的别名**：setActor 后 getRole 可读、setRole 后 getActor 可读（均落在 1.2 `env:role` 属性）；1.2 上 setActor **不抛**。
4. 1.2 的 role 属性 QName 是 `{http://www.w3.org/2003/05/soap-envelope}role`（1.1 的 actor 是 `{http://schemas.xmlsoap.org/soap/envelope/}actor`）——DOM 属性断言必须用对命名空间。
5. 默认值全为 null/false（actor/role/MU/relay）。
6. `examineAllHeaderElements()` 迭代得到的是**同一实例**（无拷贝）；`URI_SOAP_ACTOR_NEXT`、`URI_SOAP_1_2_ROLE_NEXT/NONE/ULTIMATE_RECEIVER` 常量可直接作为 actor/role 值。
7. detach 后 header 计数归零，二次 detach 不抛。

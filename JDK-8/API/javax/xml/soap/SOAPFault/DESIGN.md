# SOAPFault — 设计案

## 1. API 功能
SOAP 错误块（extends SOAPBodyElement）。1.1 面：setFaultCode(String/Name/QName)+getFaultCode/getFaultCodeAsName/getFaultCodeAsQName、setFaultString(+Locale)、setFaultActor、addDetail/getDetail/hasDetail；1.2 面：appendFaultSubcode/getFaultSubcodes/removeAllFaultSubcodes、addFaultReasonText/getFaultReasonText(s)/getFaultReasonLocales、setFaultRole/setFaultNode。

## 2. 使用方式
1.1：`body.addFault()` → `f.setFaultCode("Client")` + `setFaultString("boom")` + `addDetail()` 细化。1.2：`addFault(new QName(env12ns,"Sender"), str)` → reason 多 Locale、subcode 栈、role/node。本测试双形态全覆盖并交叉验证"版本专属 API 在另一版本上"的行为。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：1.1 code 三通道回读（限定/非限定、Name/QName 精确）、string/locale 默认与回读、actor 回读 + null 幂等、detail 通道（重复 addDetail 抛）、**1.1 上 12 个 1.2 专属 API 全抛 UnsupportedOperationException 表**、1.2 subcode 追加顺序（FIFO）与 removeAll、reason 多 Locale 增改查（含默认 Locale 无关化计数）、role/node 回读、1.2 负例（setFaultCode(String) 非标准码）。断言 96 条（旧 27）。
- 坑1：1.1 上 1.2 专属 API 抛 **UnsupportedOperationException**（"Not supported in SOAP 1.1"），非 SOAPException。
- 坑2：无参 addFault 的默认 fault string 是占位文本 "Fault string, and possibly fault code, not set"（非 null）；setFaultString(String) 后 getFaultStringLocale()==null（javadoc 明确 null locale）。
- 坑3：1.2 setFaultCode(String) 对 "Client"/"Sender" 均抛 "X is not a standard Code value"——1.2 必须走 QName/Name 通道。
- 坑4：1.2 无参 addFault 默认 code={env12}Receiver；getFaultCode() 字符串形为 "env:Receiver"（带前缀）。
- 坑5：**removeAllFaultSubcodes 之后 appendFaultSubcode 失效**（子码列表被永久破坏，RI quirk）。
- 坑6：reason 计数依赖默认 Locale（默认 Locale==ENGLISH 时 EN 添加为替换）——断言按 Locale.getDefault() 分支计算。
- 坑7：addFaultReasonText(text, null) → SOAPException "locale is required and must not be null"；getFaultReasonText(未知 Locale) 返回 null 不抛。
- 坑8：setFaultActor(null) 是 no-op（旧值保留）。

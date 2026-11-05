# javax.xml.soap.SOAPFaultElement 契约测试设计

## 1. API 功能

`SOAPFaultElement` 是 SOAP Fault 内部子结构的类型标记（marker interface，`extends SOAPElement`，无自有方法）：1.1 fault 的 `faultcode`/`faultstring` 子元素、以及 `Detail`（`Detail extends SOAPFaultElement`）都属于它。注意 `DetailEntry extends SOAPElement`（**不**是 FaultElement），`SOAPFault extends SOAPBodyElement`（也不 是 FaultElement）——层次分叉是本接口的核心考点。

## 2. 使用方式

不经工厂直接获取：`SOAPFault f = body.addFault(code, string)`，然后 `f.getChildElements()` 迭代得到的 faultcode/faultstring 即 `SOAPFaultElement`；`f.addDetail()` 得到的 `Detail` 也是。在子元素上可用全部 SOAPElement 操作（setEncodingStyle/getValue/detachNode）。

## 3. 参考来源

- Oracle javadoc Java SE 8：docs.oracle.com/javase/8/docs/api/javax/xml/soap/SOAPFaultElement.html
- JSR 67 (SAAJ 1.3)
- OpenJDK 8u RI：`com.sun.xml.internal.messaging.saaj.soap.ver1_1.FaultElement1_1Impl`、`Detail1_1Impl`、`DetailEntry1_1Impl`
- 本仓库测试：`JDK-8/API/javax/xml/soap/SOAPFaultElement/SOAPFaultElementTest.java`

## 4. 测试用例设计 + 易错规范细节

维度：fault 子元素类型与值精确锁定 → faultstring 经 fault API 与子元素双向同步 → faultcode 子元素编码风格 → Detail/DetailEntry 层次断言 → fault 自身类型断言 → 子元素 detach 语义。

易错点（实测锁定）：

1. **fault 的两个子元素（faultcode/faultstring）在 RI 中都实现 SOAPFaultElement**；faultcode 子的 value 是**带前缀限定形式**（如 `SOAP-ENV:Client`），不是裸 localName。
2. `setFaultString` 会**同步更新** faultstring 子元素的 value（经 `getChildElements` 读子节点可见）。
3. **无 Locale 的 addFault(QName,String)/setFaultString(String) 之后 `getFaultStringLocale()` 返回 null**；只有 `setFaultString(String, Locale)`/`addFault(...,Locale)` 才设置 locale。
4. `Detail instanceof SOAPFaultElement` 为 **true**（接口事实）；`DetailEntry instanceof SOAPFaultElement` 为 **false**。
5. `SOAPFault` 自身**不能** cast 成 SOAPFaultElement（RI ClassCastException）——它是 SOAPBodyElement。
6. detach faultstring 子元素后：fault 子计数减一、`body.hasFault()` 仍 true（fault 本体还在），但 `getFaultString()` 会回落到 RI 默认串——**detach 后不要断言 getFaultString 的精确值**（实现路径不稳）。

# SOAPBody — 设计案

## 1. API 功能
SOAP 体（extends SOAPElement）：业务载荷容器。addBodyElement(QName/Name)；fault 通道 addFault 四重载（无参/Name+String/Name+String+Locale/QName+String/QName+String+Locale）+ hasFault/getFault；DOM 桥 addDocument(Document)/extractContentAsDocument()。

## 2. 使用方式
`body.addBodyElement(qname).addTextNode(v)` 写载荷；`body.addFault()` 后经 SOAPFault 细化；`body.addDocument(dom)` 导入整棵 DOM 子树；`extractContentAsDocument()` 把唯一 body 子元素导出为独立 Document。本测试 1.1/1.2 双形态。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：addBodyElement 两形态回读、fault 生命周期（addFault→hasFault/getFault 同实例→重复抛→detach 复位→重建）、四重载矩阵（code/string/locale 精确回读）、addDocument 导入（root/nested 计数+文本）+ extractContentAsDocument 往返 + 多子元素负例 + 仅 fault 形态、1.2 形态（无参 addFault 默认 Receiver/QName 重载/混入 body 元素后 addFault 拒绝）。断言 52 条（旧 10）。
- 坑1：重复 addFault → SOAPException "Error: Fault already exists"；fault detachNode 后可重建。
- 坑2：extractContentAsDocument 多于一个子元素 → SOAPException "Cannot extract Document from body"；**仅含 fault 时 RI 不抛**（导出 Fault 根文档）——javadoc 未定义，锁定实际。
- 坑3：1.2 上 body 已有非 fault 子元素时 addFault → SOAPException "No other element except Fault allowed in SOAPBody"。
- 坑4：1.1 消息上允许 addFault(1.2 ns 的 QName, ...)（RI 不校验版本归属）。

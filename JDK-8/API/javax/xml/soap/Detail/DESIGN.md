# Detail — 设计案

## 1. API 功能
SOAP fault 细节容器（extends SOAPFaultElement）：addDetailEntry(QName/Name) 生成 DetailEntry，getDetailEntries() 迭代。应用程序特定错误结构的标准挂点。

## 2. 使用方式
`SOAPFault f = body.addFault(); Detail d = f.addDetail(); d.addDetailEntry(qname).addTextNode(v);` 消费方 `f.getDetail().getDetailEntries()` 逐条取。本测试走真实 fault.addDetail 通道（非 SOAPFactory.createDetail 孤儿通道）。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：空 detail、多条 entry 迭代计数 + 逐条 instanceof DetailEntry、Name/QName 双形态、entry QName 精确回读、父子关系（entry.parent==detail、detail.parent==fault、fault.getDetail() 同实例）、重复 addDetail 抛 SOAPException、entry/detail detach 后计数与 hasDetail 联动、重建。断言 27 条（旧 6）。
- 坑1：一个 fault 至多一个 detail——第二次 addDetail 抛 SOAPException。
- 坑2：detail detachNode 后 fault.hasDetail() 翻转为 false 且 getDetail() null，可再 addDetail。
- 坑3：1.2 fault 上 detail 通道同样可用。

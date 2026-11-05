# SOAPHeader — 设计案

## 1. API 功能
SOAP 头（extends SOAPElement）：带寻址/路由语义的扩展块容器。addHeaderElement(QName/Name)；examine（只读）/extract（移除）×（全部/按 actor|role）；examineMustUnderstandHeaderElements；addNotUnderstoodHeaderElement（1.2 专属）/addUpgradeHeaderElement 三重载。

## 2. 使用方式
`header.addHeaderElement(qname)` 建块，块上 setActor(1.1)/setRole(1.2)/setMustUnderstand/setRelay(1.2)；消费方 `examineHeaderElements(actor)` 只看不删、`extractHeaderElements(actor)` 取走删（中间节点语义）。

## 3. 参考来源
Oracle javadoc (docs.oracle.com/javase/8/docs/api/javax/xml/soap/)、JSR 67 (SAAJ 1.3)、OpenJDK 8u 内置 RI com.sun.xml.internal.messaging.saaj（rt.jar）、本仓库测试文件

## 4. 测试用例设计
维度：addHeaderElement 两形态回读 + 迭代元素类型；examine/extract 语义矩阵（examine 不删、extract 删、按 actor 过滤 1/0、NEXT 常量可直接作过滤器）；mustUnderstand 默认 false + 翻转 + examineMustUnderstand 过滤；1.1 role/relay 家族全抛 UnsupportedOperationException；1.2 setRole/getRole 回读 + **setActor 实测不抛**（与 role 同存储）+ setRelay 回读；addNotUnderstood 1.1 抛/1.2 成功；addUpgradeHeaderElement 三重载。断言 58 条（旧 12）。
- 坑1：默认消息自带 header——直接操作 getSOAPHeader() 即可；空 header 需 detach 重建（envelope.addHeader）。
- 坑1：**1.1 上 getRole/setRole/setRelay/getRelay/addNotUnderstoodHeaderElement 全部抛 UnsupportedOperationException**（不是 SOAPException）。
- 坑3：1.2 上 setActor 不抛且写的就是 role 属性（getRole/getActor 同值）——javadoc 暗示应为 1.2 保留，实测锁定 RI 行为。
- 坑4：examineMustUnderstandHeaderElements(actor) 只数 mustUnderstand=true 的块。
- 坑5：addUpgradeHeaderElement 生成 env 命名空间下的 Upgrade 元素。

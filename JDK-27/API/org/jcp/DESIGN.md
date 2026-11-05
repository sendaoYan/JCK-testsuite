# org.jcp（XML-DSIG 内部实现）契约覆盖设计 —— JDK-27 侧

## 1. 范围与结构
JDK-27 的 `org.jcp.xml.dsig.internal[.dom]` 共 **47** 个类/接口（比 JDK-8 多
`DOMRSAPSSSignatureMethod`）。47 个测试文件全部带 `@modules` 打开语句：
- dom 包类：`java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open`。
- 三个流类：额外 `java.xml.crypto/org.jcp.xml.dsig.internal:+open`。
- Apache 两个数据类：额外 `java.xml.crypto/com.sun.org.apache.xml.internal.security.signature:+open`
  （反射构造 XMLSignatureInput 适配器需要）。

## 2. 契约覆盖方法
与 JDK-8 侧同一三层结构（形态锁/成员面锁/黑盒行为），断言集在 8 侧全部成立外，27 侧全部同样成立
（双侧探针逐一验证），差异点见第 3 节。亮点补充：
- ApacheNodeSetData：Node 输入 → 迭代器恰 1 个节点；byte 输入 → 迭代即 RuntimeException。
- DOMXMLSignature/DOMSignatureValue：五参构造的 SignatureValue id 往返、签名前 getValue()==null、
  实现类型精确为 DOMXMLSignature$DOMSignatureValue。
- DOMRSAPSSSignatureMethod：存在性锁 + “默认 PSS 连线仍是旧 DOMSignatureMethod 家族”的反直觉锁。

## 3. 版本差异锁（与 JDK-8 侧互锁）
- `DOMRSAPSSSignatureMethod` 仅 27 侧存在（8 侧 CNFE 断言，27 侧 forName 成功断言）。
- `Policy.disabledAlgs()`：27 侧 10 项（新增 sha1、rsa-sha1、dsa-sha1、ecdsa-sha1、MGF1、xpath 等）。

## 4. 运行
JDK-27 jtreg（`-jdk jdk-27.0.1.0.2+1`）：47/47 通过，总断言 1234（基线 669，+565）。

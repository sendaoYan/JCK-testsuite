# org.jcp（XML-DSIG 内部实现）契约覆盖设计 —— JDK-8 侧

## 1. 范围与结构
JDK-8 的 `org.jcp.xml.dsig.internal[.dom]` 共 **46** 个类/接口（权威清单 `tools/enumerate_api.py` 比对 rt.jar），
本侧 46 个测试文件（45 个目录 + DOMXMLSignature 目录下 DOMSignatureValue 子测试；
`AbstractDOMSignatureMethod` 为抽象骨架，无独立测试目录）。
包结构：
- `org.jcp.xml.dsig.internal`：DigesterOutputStream、MacOutputStream、SignerOutputStream（流式摘要/MAC/签名）。
- `org.jcp.xml.dsig.internal.dom`：DOM* 家族（结构/引用/签名对象）、Apache*（Canonicalizer/Transform 基类与数据适配）、
  Policy（安全策略哨兵）、Utils/DOMUtils（工具）、XMLDSigRI（Provider）。

## 2. 契约覆盖方法（每类三层）
1. **形态锁**：超类/直接接口集合/修饰符集合的精确断言（含 final/abstract 组合）。
2. **成员面锁**：public/protected 声明方法名集合（静态成员名单独锁）与构造器参数类型序列
   （`Class.getName()` 串接；byte[] → `[B`；私有构造器不计入，与权威清单口径一致）。
3. **黑盒行为**（双侧逐一探针验证后才入档）：
   - 值语义：DOMKeyName/DOMX509IssuerSerial/DOMCryptoBinary 的 equals/hashCode/往返；
     DOMCryptoBinary 新实例不等（equality 需编组节点）。
   - 不可变视图：DOMManifest/DOMKeyInfo/DOMX509Data/DOMSignatureProperties/DOMSignatureProperty/DOMXMLObject/
     DOMReference/DOMSignedInfo/DOMXMLSignature 的列表 → UnsupportedOperationException。
   - 精确 IAE/NPE 消息：空引用表（"list of references must contain at least one entry"）、空内容
     （"content cannot be empty"/"properties cannot be empty"）、DOMRetrievalMethod null URI（"uri cannot be null"）、
     DOMURIDereferencer null uriRef（"uriRef cannot be null"）、DOMPGPData 四条精确消息
     （bit 7 / 至少 3 字节 / keyId 8 字节 / 合法 0xC5）。
   - Transform 矩阵：8 个 transform/c14n 类的 init(null)/init(错配 spec)（InvalidAlgorithmParameterException 精确分派）、
     isFeatureSupported(null)→NPE、marshalParams(null,null)→NPE、fresh alg/provider/spec 全 null。
   - 流闭环：DigesterOutputStream 写入字节 == 独立 MessageDigest；MacOutputStream 的 mac.doFinal == 独立 MAC；
     SignerOutputStream 的 sig.sign == 独立签名且可验签（RSA 2048）。
   - Provider 锁：XMLDSigRI 名 "XMLDSig"、info 前缀、13 服务（11 TransformService + 两工厂）、
     Security.getProvider 类同一性；46 类逐一 Class.forName 可达。
   - Policy 哨兵：maxTransforms 5/maxReferences 30/minKeySize RSA 1024、restrict* 矩阵、
     disabledAlgs == 4（md5/rsa-md5/xslt/hmac-md5）、disabledReferenceUriSchemes 含 file/http/https。
   - 工厂闭环：DOM 工厂产出 DOM* 实例（DigestMethod/SignatureMethod/HMAC/KeyValue/Reference/SignedInfo/
     KeyInfo/URIDereferencer）；直接 `new DOMKeyInfoFactory()` 的 getMechanismType 为 null（注册驱动）。
   - 工具行为：DOMUtils 的 create/attr/ID/qname/遍历/paramsEqual(null,null)/nodesEqual(自)；
     Utils.readBytesFromStream 往返、parseIdFromSameDocumentURI 空串→null。

## 3. 版本差异锁（与 JDK-27 侧互锁）
- `DOMRSAPSSSignatureMethod`：**8 侧不存在**（XMLDSigRI 清单里断言 ClassNotFoundException）；27 侧存在且
  为 AbstractDOMSignatureMethod 的另一子族，但默认 PSS 工厂连线仍是旧 DOMSignatureMethod 家族。
- `Policy.disabledAlgs()`：8 侧 4 项 vs 27 侧 10 项（含 sha1/rsa-sha1/ecdsa-sha1/xpath 等）。

## 4. 运行
JDK-8 jtreg（`JTREG_JAVA=jdk-17.0.7+7`，`-jdk jdk8u372-b07`）：46/46 通过，
总断言 1186（基线 630，+556）。

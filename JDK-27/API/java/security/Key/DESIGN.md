# Key — 设计案

## 1. API 功能
密钥接口（extends Serializable）。getAlgorithm/getFormat/getEncoded。

## 2. 使用方式
通过 KeyPairGenerator 生成的公/私钥实现。

## 3. 参考来源
- javadoc `java.security.Key`。
- JCK runtime-8b `api/java_security/Key/`。

## 4. 测试用例设计 + 易错规范细节
- 接口 extends Serializable；Key.serialVersionUID 定义。
- RSA 公钥 X.509 / 私钥 PKCS#8、算法 RSA、getEncoded 非平凡。
- 公钥序列化往返后 getAlgorithm/getFormat/getEncoded/equals 保持。

## 5. 契约深化补充（本批）
- 私钥序列化（KeyRep PKCS#8 路径）round-trip equals/编码相等/hashCode 稳定；DSA 公钥同；跨算法钥匙永不相等（对称）。
- getEncoded 每次返回防御拷贝（引用不等且值相等）。
- KeyFactory 由编码重建的钥匙与原钥匙 equals；PublicKey/PrivateKey 均 extends Key。

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

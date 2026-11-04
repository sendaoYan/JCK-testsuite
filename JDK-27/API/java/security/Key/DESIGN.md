# java.security.Key 黑盒行为测试设计

## 1. API 功能
- 接口 `Key extends Serializable`：`getAlgorithm()`、`getFormat()`、`getEncoded()`。

## 2. Java 用户如何使用
任何密钥（公钥/私钥/对称密钥）都实现 Key，通过 `getAlgorithm()`、`getFormat()`（如 "X.509"/"PKCS#8"/"RAW"）、`getEncoded()` 取算法与编码字节。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/KeyFactory/GenerateRSAPrivateCrtKey.java`：对 Key 调用 getEncoded/getAlgorithm。
- 实现权威：`src/java.base/share/classes/java/security/Key.java`。

## 4. 测试用例设计 + 自动化实现
- 真实 RSA 公钥：instanceof Key、getAlgorithm "RSA"、getFormat "X.509"、getEncoded 非空且为防御性拷贝；私钥 format "PKCS#8"，公/私钥编码互异。
- 白盒：接口 + Serializable，含 getAlgorithm/getEncoded/getFormat。

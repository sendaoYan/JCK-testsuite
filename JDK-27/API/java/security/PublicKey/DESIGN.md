# java.security.PublicKey 黑盒行为测试设计

## 1. API 功能
- 接口 `PublicKey extends AsymmetricKey`（AsymmetricKey extends Key + BinaryEncodable），含 `serialVersionUID`。
- 继承 Key 的 `getAlgorithm/getFormat/getEncoded`；继承 AsymmetricKey 的默认方法 `getParams()`。

## 2. Java 用户如何使用
从 `KeyPairGenerator.generateKeyPair().getPublic()` 取得，做 `getAlgorithm()/getFormat()/getEncoded()`；JDK-27 新增 `getParams()`（默认 null）。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/KeyFactory/...` 与大量 crypto 测试对公钥调 getEncoded/getAlgorithm。
- 实现权威：`src/java.base/share/classes/java/security/PublicKey.java`、`AsymmetricKey.java`。

## 4. 测试用例设计 + 自动化实现
- 真实 RSA 公钥：instanceof PublicKey/AsymmetricKey/Key；getAlgorithm "RSA"、getFormat "X.509"、getEncoded 非空；`getParams()` 默认 null。
- 白盒：接口，父接口为 AsymmetricKey（字符串反射断言）。

## 5. 追加契约维度（深化批）
- marker 形状：无声明方法、sVUID=7187392471159151072L（反射精确值）。
- KeyFactory 编码 round-trip：generatePublic(X509(encode)) equals 原钥、hashCode 一致、编码相等。
- DSA-1024 公钥 X.509；KeyRep 序列化 round-trip equals。
- 自定义 opaque 钥 null format/encoding 合法。

# java.security.KeyPairGenerator 黑盒行为测试设计

## 1. API 功能
- 工厂：`getInstance(String)`、`getInstance(String,String)`、`getInstance(String,Provider)`。
- 初始化：`initialize(int)`、`initialize(int,SecureRandom)`、`initialize(AlgorithmParameterSpec)`、`initialize(AlgorithmParameterSpec,SecureRandom)`。
- 生成：`genKeyPair()`（final）、`generateKeyPair()`；查询 `getAlgorithm()`、`getProvider()`。

## 2. Java 用户如何使用
`KeyPairGenerator.getInstance("RSA")` → `initialize(2048)` → `generateKeyPair()` → `getPublic()/getPrivate()`；EC 用 `initialize(new ECGenParameterSpec("secp256r1"))`。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/KeyPairGenerator/GenerateKeypair.java`：RSA/EC 生成密钥对。
- `test/jdk/java/security/KeyPairGenerator/GenerateRSAKeyPair.java`：RSA 参数化生成。
- 实现权威：`src/java.base/share/classes/java/security/KeyPairGenerator.java`。

## 4. 测试用例设计 + 自动化实现
- RSA 2048：算法名、provider、公/私钥非空、format/encoded 非空；`genKeyPair()` 与 `generateKeyPair()` 均可用。
- EC secp256r1：密钥对生成 + 公钥编码非空。
- 白盒：abstract extends KeyPairGeneratorSpi，含 getInstance/initialize/genKeyPair/generateKeyPair/getAlgorithm/getProvider。

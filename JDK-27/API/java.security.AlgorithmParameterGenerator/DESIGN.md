# java.security.AlgorithmParameterGenerator 黑盒行为测试设计

## 1. API 功能
- 工厂：`getInstance(String)`、`getInstance(String,String)`、`getInstance(String,Provider)`。
- 初始化：`init(int)`、`init(int,SecureRandom)`、`init(AlgorithmParameterSpec)`、`init(AlgorithmParameterSpec,SecureRandom)`。
- 生成：`generateParameters()`；查询 `getAlgorithm()`、`getProvider()`。

## 2. Java 用户如何使用
`AlgorithmParameterGenerator.getInstance("DSA")` → `init(2048)` → `generateParameters()` 得到 AlgorithmParameters，再 `getParameterSpec(DSAParameterSpec.class)`。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/...` 及 `test/jdk/com/sun/crypto/provider/...` 用 `AlgorithmParameterGenerator` 生成 DH/DSA 参数。
- 实现权威：`src/java.base/share/classes/java/security/AlgorithmParameterGenerator.java`。

## 4. 测试用例设计 + 自动化实现
- DSA：`init(2048)` 后 `generateParameters()` 非空、算法名 "DSA"；`getParameterSpec(DSAParameterSpec.class)` 的 p/q/g 非空且 p 位长 2048。
- `init(int,SecureRandom)` 变体、显式 provider getInstance。
- 白盒：concrete，含 getAlgorithm/getInstance/getProvider/init/generateParameters。
- 注：JDK-27 中 "AES"/"EC" 无 AlgorithmParameterGenerator（实测 NoSuchAlgorithmException），故用 "DSA"。

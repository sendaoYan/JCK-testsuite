# java.security.AlgorithmParameters 黑盒行为测试设计

## 1. API 功能
- 工厂：`getInstance(String)`、`getInstance(String,String)`、`getInstance(String,Provider)`。
- 初始化：`init(AlgorithmParameterSpec)`、`init(byte[])`、`init(byte[],String)`。
- 转换：`getParameterSpec(Class)`、`getEncoded()`、`getEncoded(String)`。
- 查询：`getAlgorithm()`、`getProvider()`、`toString()`。

## 2. Java 用户如何使用
`AlgorithmParameters.getInstance("AES")` → `init(new IvParameterSpec(iv))` → `getEncoded()` 或 `getParameterSpec(IvParameterSpec.class)` 取回参数。

## 3. 开源代码实际用法调研
- `test/jdk/javax/crypto/.../ParametersTest` 等大量用例以 `AlgorithmParameters.getInstance` + `getParameterSpec` 取参数（AES/DES/GCM）。
- 实现权威：`src/java.base/share/classes/java/security/AlgorithmParameters.java`。

## 4. 测试用例设计 + 自动化实现
- AES：`init(IvParameterSpec)` 后 `getParameterSpec(IvParameterSpec.class)` 的 IV 与原 IV 一致；`getEncoded()` 非空且 `init(byte[])` 后再取参数一致；`getEncoded("ASN.1")` 非空。
- getAlgorithm/getProvider/toString 非空；显式 provider getInstance。
- 白盒：concrete，含 getAlgorithm/getInstance/getProvider/init/getParameterSpec/getEncoded/toString。

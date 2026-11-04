# java.security.SecureRandom 黑盒行为测试设计

## 1. API 功能
- 构造：`SecureRandom()`、`SecureRandom(byte[] seed)`。
- 工厂：`getInstance(String)`、`getInstance(String,String)`、`getInstance(String,Provider)`、`getInstanceStrong()`。
- 核心操作：`nextBytes(byte[])`、`nextBytes(byte[],SecureRandomParameters)`、`nextLong()`、`generateSeed(int)`、`getSeed(int)`。
- 状态：`setSeed(byte[])`、`setSeed(long)`、`reseed()`、`reseed(params)`；查询 `getAlgorithm()`、`getProvider()`、`getParameters()`。

## 2. Java 用户如何使用
`new SecureRandom()` 或 `SecureRandom.getInstance("DRBG")`，然后 `nextBytes(buf)` 填充随机字节；需要强随机用 `getInstanceStrong()`；需要生成种子用 `generateSeed(n)`。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/SecureRandom/ApiTest.java`：各公开方法调用。
- `test/jdk/java/security/SecureRandom/GetInstanceTest.java`：getInstance 各重载。
- `test/jdk/java/security/SecureRandom/Serialize.java`：序列化行为。
- 实现权威：`src/java.base/share/classes/java/security/SecureRandom.java`。

## 4. 测试用例设计 + 自动化实现
- 注：JDK-27 默认 NativePRNG 会混入 OS 熵，`new SecureRandom(byte[])` 两次实例**不保证**相同输出序列（实测不相等），因此不按任务原设想断言“固定种子两次实例序列相等”。改为断言确定性可证的性质：nextBytes 填满指定长度、generateSeed/getSeed 返回精确长度、setSeed 不抛异常、getInstance/getInstanceStrong 非空可 nextBytes。
- 白盒：extends java.util.Random，含 getInstance/getProvider/getAlgorithm/setSeed/nextBytes/generateSeed/getSeed/getInstanceStrong/reseed 等方法。

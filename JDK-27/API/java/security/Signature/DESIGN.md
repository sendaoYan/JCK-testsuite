# java.security.Signature 黑盒行为测试设计

## 1. API 功能
- 工厂：`getInstance(String)`、`getInstance(String,String)`、`getInstance(String,Provider)`。
- 初始化：`initSign(PrivateKey)`、`initSign(PrivateKey,SecureRandom)`、`initVerify(PublicKey)`、`initVerify(Certificate)`。
- 操作：`update(byte)`、`update(byte[])`、`update(byte[],int,int)`、`update(ByteBuffer)`；`sign()`、`sign(byte[],int,int)`、`verify(byte[])`、`verify(byte[],int,int)`。
- 查询：`getAlgorithm()`、`getProvider()`、`getParameters()`；`clone()`。

## 2. Java 用户如何使用
`Signature.getInstance("SHA256withRSA")` → `initSign(priv)` → `update(data)` → `sign()`；验证端 `initVerify(pub)` → `update(data)` → `verify(sig)`。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/Signature/SignWithOutputBuffer.java`：`sign(byte[],int,int)` 输出缓冲。
- `test/jdk/java/security/Signature/ByteBuffers.java`：`update(ByteBuffer)`。
- `test/jdk/java/security/Signature/SignatureGetInstance.java`：getInstance 用法。
- 实现权威：`src/java.base/share/classes/java/security/Signature.java`。

## 4. 测试用例设计 + 自动化实现
- RSA SHA256withRSA：sign/verify 往返、offset update、`sign(byte[],int,int)` 写入缓冲、ByteBuffer update、篡改数据验证失败。
- EC SHA256withECDSA（secp256r1）：sign/verify 往返。
- 白盒：abstract extends SignatureSpi，含 getInstance/initSign/initVerify/update/sign/verify/clone。
- 注：`Signature$Delegate.clone()` 抛 CloneNotSupportedException，故黑盒不断言 clone 成功（仅白盒断言方法存在）。

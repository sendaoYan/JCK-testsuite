# java.security.MessageDigest 黑盒行为测试设计

## 1. API 功能
- 工厂：`getInstance(String)` / `getInstance(String,String)` / `getInstance(String,Provider)`（算法名 + 可选 provider）。
- 核心操作：`update(byte)`、`update(byte[],int,int)`、`update(byte[])`、`update(ByteBuffer)`；`digest()`、`digest(byte[],int,int)`、`digest(byte[])`。
- 查询：`getAlgorithm()`、`getDigestLength()`、`getProvider()`；`isEqual(byte[],byte[])`（常数时间比较）。
- 状态管理：`reset()`、`clone()`；`toString()`。

## 2. Java 用户如何使用
典型组合链路：`getInstance("SHA-256")` → 多次 `update(...)` 分片喂入 → `digest()` 取摘要；或一次性 `digest(byte[])`。校验用 `MessageDigest.isEqual(a, b)`。重置用 `reset()`，复制用 `clone()`。
```java
MessageDigest md = MessageDigest.getInstance("SHA-256");
md.update(data, 0, data.length);
byte[] digest = md.digest();
if (!MessageDigest.isEqual(digest, expected)) { ... }
```

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/java/security/MessageDigest/TestDigestIOStream.java`：DigestInputStream/DigestOutputStream 与 MessageDigest 组合。
- `test/jdk/java/security/MessageDigest/ByteBuffers.java`：`update(ByteBuffer)` 用法。
- `test/jdk/java/security/MessageDigest/ArgumentSanity.java`：`update`/`digest` 边界参数。
- 实现权威：`src/java.base/share/classes/java/security/MessageDigest.java`。

## 4. 测试用例设计 + 自动化实现
- 已知向量：SHA-256("abc")、SHA-256("")、MD5("abc") 固定 hex 串断言。
- 分片等价：逐字节 update、offset update 与一次性 digest 结果相等。
- digest 数组往返：`digest(byte[],int,int)` 写入缓冲区并返回长度。
- `reset()`、`clone()` 独立性、`isEqual` 边界（null、长度不等）。
- `getAlgorithm`/`getDigestLength`/`getProvider`/显式 provider `getInstance`。

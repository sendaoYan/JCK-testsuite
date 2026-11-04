# java.security.DigestInputStream 黑盒行为测试设计

## 1. API 功能
- 构造：`DigestInputStream(InputStream, MessageDigest)`。
- 查询/配置：`getMessageDigest()`、`setMessageDigest(MessageDigest)`、`on(boolean)`。
- 继承 FilterInputStream 的 `read()`、`read(byte[],int,int)`；`toString()`。

## 2. Java 用户如何使用
边读边摘要：默认 on=true，读字节同时更新 digest；`on(false)` 暂停更新；`setMessageDigest` 换摘要算法。
```java
DigestInputStream dis = new DigestInputStream(in, MessageDigest.getInstance("SHA-256"));
while (dis.read() != -1) { }
byte[] digest = dis.getMessageDigest().digest();
```

## 3. 开源代码实际用法调研
- `test/jdk/java/security/MessageDigest/TestDigestIOStream.java`：DigestInputStream 与 DigestOutputStream 组合读写摘要。
- 实现权威：`src/java.base/share/classes/java/security/DigestInputStream.java`。

## 4. 测试用例设计 + 自动化实现
- 逐字节 read 后 digest == SHA-256("abc")；on(false) 不更新；setMessageDigest 替换后生效；getMessageDigest/toString 断言。
- 白盒：extends FilterInputStream，含 getMessageDigest/setMessageDigest/read/on/toString。

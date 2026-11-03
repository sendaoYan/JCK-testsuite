# java.security.DigestOutputStream 黑盒行为测试设计

## 1. API 功能
- 构造：`DigestOutputStream(OutputStream, MessageDigest)`。
- 查询/配置：`getMessageDigest()`、`setMessageDigest(MessageDigest)`、`on(boolean)`。
- 继承 FilterOutputStream 的 `write(int)`、`write(byte[],int,int)`；`toString()`。

## 2. Java 用户如何使用
边写边摘要：默认 on=true，写出字节同时更新 digest；`on(false)` 暂停；`setMessageDigest` 换算法。
```java
DigestOutputStream dos = new DigestOutputStream(out, MessageDigest.getInstance("SHA-256"));
dos.write(data);
byte[] digest = dos.getMessageDigest().digest();
```

## 3. 开源代码实际用法调研
- `test/jdk/java/security/MessageDigest/TestDigestIOStream.java`：DigestOutputStream 用法。
- 实现权威：`src/java.base/share/classes/java/security/DigestOutputStream.java`。

## 4. 测试用例设计 + 自动化实现
- 逐字节 write 后 digest == SHA-256("abc")；on(false) 不更新；setMessageDigest 替换后生效；getMessageDigest/toString 断言。
- 白盒：extends FilterOutputStream，含 getMessageDigest/setMessageDigest/write/on/toString。

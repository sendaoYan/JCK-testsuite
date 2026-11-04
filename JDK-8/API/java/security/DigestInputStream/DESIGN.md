# DigestInputStream — 设计案

## 1. API 功能
过滤输入流，读数据同时更新摘要。构造器(InputStream, MessageDigest) + getMessageDigest/setMessageDigest/on(boolean) + 继承 FilterInputStream 的 read。

## 2. 使用方式
`new DigestInputStream(in, MessageDigest.getInstance("MD5"))` 默认 on，读完后 `getMessageDigest().digest()`。

## 3. 参考来源
- javadoc `java.security.DigestInputStream`。
- JCK runtime-8b `api/java_security/DigestInputStream/`。

## 4. 测试用例设计 + 易错规范细节
- 默认 on：read(byte[]) 累积；read(byte[],off,len) 只累积读取字节。
- on(false) 不累积、on(true) 重新累积。
- setMessageDigest 替换后累积到新摘要。
- 易错点：类在 java.security 而非 java.io；构造器不校验 null 流/摘要（读时才 NPE）。

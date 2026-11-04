# DigestOutputStream — 设计案

## 1. API 功能
过滤输出流，写数据同时更新摘要。构造器(OutputStream, MessageDigest) + getMessageDigest/setMessageDigest/on(boolean) + 继承 FilterOutputStream 的 write。

## 2. 使用方式
`new DigestOutputStream(out, MessageDigest.getInstance("MD5"))` 默认 on，写完后 `getMessageDigest().digest()`。

## 3. 参考来源
- javadoc `java.security.DigestOutputStream`。
- JCK runtime-8b `api/java_security/DigestOutputStream/`。

## 4. 测试用例设计 + 易错规范细节
- 默认 on：write(byte[]) 累积；write(int) 逐字节累积。
- on(false) 不累积、on(true) 重新累积。
- setMessageDigest 替换后累积到新摘要。
- 易错点：类在 java.security 而非 java.io；on 状态只影响摘要更新，不影响底层流写入。

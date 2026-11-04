# javax.net.ssl.SSLSessionContext 黑盒行为测试设计

## 1. API 功能
接口：`getSession(byte[])`、`getIds()`、`set/getSessionTimeout`、`set/getSessionCacheSize`。

## 2. Java 用户如何使用
`SSLContext.getClientSessionContext()/getServerSessionContext()` 管理会话缓存。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/SSLSession/` 覆盖会话缓存。

## 4. 测试用例设计 + 自动化实现
黑盒：timeout/cacheSize 往返；真实握手后 getIds 非空、getSession(id) 解析。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。

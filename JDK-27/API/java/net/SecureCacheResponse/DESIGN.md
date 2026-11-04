# SecureCacheResponse 设计文档

## 一、API 功能

CacheResponse 的 HTTPS 安全子类，额外暴露 TLS 会话信息。

## 二、Java 用户如何使用

用户继承实现 getSSLEngine() 与 getLocalCertificateChain()。

## 三、开源代码实际用法调研

缓存 HTTPS 响应时提供证书链信息。

## 四、测试用例设计

验证 abstract 继承 CacheResponse；声明 getSSLEngine/getLocalCertificateChain。

## 五、契约深化补充（批次 D，探针验证于 JDK-27）

**已深化断言（47 条）覆盖：**
- 结构：直接继承 CacheResponse；五个安全访问器全抽象；getServerCertificateChain/getPeerPrincipal 声明 SSLPeerUnverifiedException。
- **JDK-9+ 契约：getSSLSession() 为具体默认方法，返回 `Optional<SSLSession>`，未覆写时恒为 `Optional.empty()`；子类可覆写。** 公开方法集 8 个（JDK-8 为 7：无此方法）。
- 访问器往返：证书链实例保真（==）、Principal 实例与名字、SSLPeerUnverifiedException 精确传播（消息保真）。
- Certificate 桩仅实现 getEncoded/getPublicKey/toString/hashCode + 两个 verify（X509Certificate 桩需实现全部抽象方法，过重）。

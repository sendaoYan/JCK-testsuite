# SecureCacheResponse 设计文档

## 一、API 功能

CacheResponse 的 HTTPS 安全子类，额外暴露 TLS 会话信息。

## 二、Java 用户如何使用

用户继承实现 getSSLEngine() 与 getLocalCertificateChain()。

## 三、开源代码实际用法调研

缓存 HTTPS 响应时提供证书链信息。

## 四、测试用例设计

验证 abstract 继承 CacheResponse；声明 getSSLEngine/getLocalCertificateChain。

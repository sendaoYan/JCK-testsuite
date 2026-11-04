# SecureCacheResponse 设计文档（JDK-8 侧）

## 一、API 功能

CacheResponse 的 TLS 专用子类：额外暴露 cipher suite、双向证书链与 Principal。

## 二、Java 用户如何使用

由 ResponseCache.get 在 https 命中时返回；调用方读 TLS 会话属性。

## 三、开源代码实际用法调研

https 连接复用/会话恢复场景的缓存命中层。

## 四、测试用例设计

白盒结构（五个安全访问器抽象）、SSLPeerUnverifiedException 声明、访问器往返、证书链/Principal 实例保真。

## 五、契约深化补充（批次 D，探针验证于 JDK-8）

**已深化断言（47 条）覆盖：** 直接继承 CacheResponse；getCipherSuite/getLocalCertificateChain/getServerCertificateChain/getPeerPrincipal/getLocalPrincipal 全抽象；peer 访问器声明 SSLPeerUnverifiedException；证书链实例保真（==）、Principal 实例与名字往返、异常消息保真；Object 契约。Certificate 桩仅实现 4 抽象方法 + 2 verify。

**易错细节（JDK-8 与 JDK-27 存在差异）：**
- **JDK-8 无 getSSLSession()**（JDK-9 加入）：测试断言 `getMethod("getSSLSession")` 抛 NoSuchMethodException；公开方法集 7 个（JDK-27 为 8 个，且该方法为具体默认返回 Optional.empty()）。

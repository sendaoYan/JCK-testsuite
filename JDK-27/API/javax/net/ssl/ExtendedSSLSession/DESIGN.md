# javax.net.ssl.ExtendedSSLSession 黑盒行为测试设计

## 1. API 功能
抽象类 implements SSLSession：`getLocalSupportedSignatureAlgorithms`/`getPeerSupportedSignatureAlgorithms`、`getRequestedServerNames`、`getStatusResponses`、`exportKeyingMaterialKey(keyAlg,label,context,length)`、`exportKeyingMaterialData(label,context,length)`（RFC 5705/8446 EKM）。

## 2. Java 用户如何使用
`(ExtendedSSLSession) socket.getSession()` 后导出 keying material 或读签名算法/SNI。

## 3. 开源代码实际用法调研
JDK `test/jdk/javax/net/ssl/ExtendedSSLSession/ExportKeyingMaterialTests.java`（bug 8341346）覆盖 exporter 相等性/长度/标签。

## 4. 测试用例设计 + 自动化实现
黑盒：本地签名算法非空、requested server names；exporter data 不同 label/context 产生不同 material、客户端/服务端相同；key 的 getEncoded==data；null/空 keyAlg、null label、零长度异常。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。

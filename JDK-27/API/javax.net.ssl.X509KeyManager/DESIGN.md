# javax.net.ssl.X509KeyManager 黑盒行为测试设计

## 1. API 功能
接口 extends KeyManager：getClientAliases/chooseClientAlias/getServerAliases/chooseServerAlias/getCertificateChain/getPrivateKey。

## 2. Java 用户如何使用
KMF 返回的 KeyManager 转 X509KeyManager，按别名取私钥/证书链。

## 3. 开源代码实际用法调研
JDK `X509KeyManagerImpl` 实现。

## 4. 测试用例设计 + 自动化实现
黑盒：真实 KMF 后 chooseServerAlias 非空、证书链/私钥、server/client aliases、无 EC alias 返回 null、bogus alias 返回 null。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。

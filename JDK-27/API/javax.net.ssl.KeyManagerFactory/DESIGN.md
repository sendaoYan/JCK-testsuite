# javax.net.ssl.KeyManagerFactory 黑盒行为测试设计

## 1. API 功能
具体类：`getDefaultAlgorithm()`（SunX509）、`getInstance(...)`、`getAlgorithm`/`getProvider`、`init(KeyStore,char[])`/`init(ManagerFactoryParameters)`、`getKeyManagers()`。init 前 getKeyManagers 抛 IllegalStateException。

## 2. Java 用户如何使用
`KeyManagerFactory.getInstance(getDefaultAlgorithm())` + `init(ks, pass)` + `getKeyManagers()`。

## 3. 开源代码实际用法调研
JDK `~/git/jdk25u-dev/src/java.base/share/classes/sun/security/ssl/KeyManagerFactoryImpl.java`。

## 4. 测试用例设计 + 自动化实现
黑盒：默认算法；未知算法 NSAE；init 前 IllegalStateException；init(KeyStore) 后返回 X509ExtendedKeyManager；null KeyStore 可 init；SunX509 拒绝通用 MFP。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。

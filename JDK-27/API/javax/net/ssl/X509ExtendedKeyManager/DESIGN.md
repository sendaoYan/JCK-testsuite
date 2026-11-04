# javax.net.ssl.X509ExtendedKeyManager 黑盒行为测试设计

## 1. API 功能
抽象类 implements X509KeyManager：新增 `chooseEngineClientAlias`/`chooseEngineServerAlias`（SSLEngine 场景）。

## 2. Java 用户如何使用
KMF 返回的 KeyManager 即该类型，供 SSLEngine 选择别名。

## 3. 开源代码实际用法调研
JDK `X509KeyManagerImpl` extends X509ExtendedKeyManager。

## 4. 测试用例设计 + 自动化实现
黑盒：真实 KMF 后 chooseEngineServerAlias/chooseEngineClientAlias 非空。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。

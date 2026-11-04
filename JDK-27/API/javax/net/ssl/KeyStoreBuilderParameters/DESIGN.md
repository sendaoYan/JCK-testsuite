# javax.net.ssl.KeyStoreBuilderParameters 黑盒行为测试设计

## 1. API 功能
实现 ManagerFactoryParameters：`(KeyStore.Builder)` / `(List<KeyStore.Builder>)`；`getParameters()` 返回 unmodifiable List。

## 2. Java 用户如何使用
`new KeyStoreBuilderParameters(KeyStore.Builder.newInstance(...))` 传给 KMF.init。

## 3. 开源代码实际用法调研
JDK `KeyManagerFactoryImpl$X509` 校验并消费 KeyStoreBuilderParameters。

## 4. 测试用例设计 + 自动化实现
黑盒：单/多 builder 往返、unmodifiable、null NPE。

断言落在 `<Simple>Test.java` 的 `testBlackBox()` 方法中；`testWhiteBox()` 保留结构反射断言。

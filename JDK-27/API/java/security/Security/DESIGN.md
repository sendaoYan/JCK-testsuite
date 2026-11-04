# java.security.Security 黑盒行为测试设计

## 1. API 功能
- Provider 发现：`getProviders()`、`getProviders(String filter)`、`getProvider(String name)`。
- 属性读取：`getProperty(String)`、`getAlgorithmProperty(String,String)`、`getAlgorithms(String)`。
- （写入类 `insertProviderAt`/`addProvider`/`removeProvider`/`setProperty` 会改全局状态，本测试不调用。）

## 2. Java 用户如何使用
`Security.getProviders()` 枚举已安装 provider；`Security.getProvider("SUN")` 取指定 provider；`Security.getAlgorithms("MessageDigest")` 查可用算法集。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/Security/ProviderFiltering.java`：getProviders(filter) 用法。
- `test/jdk/java/security/Security/CaseInsensitiveAlgNames.java`：算法名大小写。
- 实现权威：`src/java.base/share/classes/java/security/Security.java`。

## 4. 测试用例设计 + 自动化实现
- getProviders 非空（不断言具体数量）；getProvider("SUN"/"SunRsaSign"/"SunJCE") 非空。
- getProperty("security.provider.1") 非空；getAlgorithms("MessageDigest") 含 SHA-256；getProviders("MessageDigest.SHA-256") 非空。
- 白盒：final 类，含 getAlgorithmProperty/getProviders/getProvider/getProperty/getAlgorithms。
- 注：`getAlgorithmProperty("MessageDigest","SHA-256")` 与 `getProperty("MessageDigest.SHA-256")` 在 JDK-27 返回 null（无该静态属性），故不对此断言非空。

# java.security.Provider 黑盒行为测试设计

## 1. API 功能
- 继承 `java.util.Properties`，另提供 `getName()`、`getVersion()`、`getVersionStr()`、`getInfo()`。
- 服务发现：`getService(String type, String algorithm)`、`getServices()`。
- 属性读取：`getProperty(String)`（只读），以及 `configure(String)`、`isConfigured()`（JDK-27 默认不支持，抛 UnsupportedOperationException）。

## 2. Java 用户如何使用
`Provider p = Security.getProvider("SUN")` → `p.getService("MessageDigest", "SHA-256")` 拿到 Service 再 `newInstance`；`p.getName()/getVersion()/getInfo()` 取元信息。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/Provider/GetInstance.java`：getService/getServices 用法。
- `test/jdk/java/security/Provider/ProviderInfoCheck.java`：getName/getVersion/getInfo 校验。
- 实现权威：`src/java.base/share/classes/java/security/Provider.java`。

## 4. 测试用例设计 + 自动化实现
- 从 Security 取 "SUN"/"SunRsaSign" provider：getName/getVersion/getInfo/toString 非空或满足契约；getService(MessageDigest,SHA-256) 非空且 type/algorithm/className/provider 一致；getServices 非空；getProperty(MessageDigest.SHA-256) 非空；SunRsaSign 提供 SHA256withRSA。
- 白盒：abstract extends Properties，含 getName/getVersion/getInfo/getService/getServices/getProperty/toString。

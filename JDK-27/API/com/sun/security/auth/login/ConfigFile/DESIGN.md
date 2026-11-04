# com.sun.security.auth.login.ConfigFile 测试设计案

## 1. API 功能
`ConfigFile` 继承 `javax.security.auth.login.Configuration`，是 JAAS 默认配置实现。JDK-27 javap 实测：
- 构造器 `ConfigFile()` 与 `ConfigFile(java.net.URI)`。
- `AppConfigurationEntry[] getAppConfigurationEntry(String)`：按应用名取条目，未配置返回 `null`。
- `void refresh()`：重读配置。
- 继承 `getType()`（`Configuration.getType()` 返回 `null`，未覆盖）。

## 2. Java 用户如何使用
```java
ConfigFile cf = new ConfigFile(uri);
AppConfigurationEntry[] e = cf.getAppConfigurationEntry("jckTestApp");
String module = e[0].getLoginModuleName();
AppConfigurationEntry.LoginModuleControlFlag flag = e[0].getControlFlag();
```

## 3. 开源代码实际用法调研
- 实现基准：`~/git/jdk25u-dev/src/jdk.security.auth/share/classes/com/sun/security/auth/login/ConfigFile.java`（委托 `sun.security.provider.ConfigFile$Spi`）。
- JDK 回归：`~/git/jdk25u-dev/test/jdk/com/sun/security/auth/login/ConfigFile/{Override,EmptyOption,PropertyExpansion,InnerClassConfig}.java`。
（网络不可用，以 JDK 源码 + test/jdk 为权威来源。）

## 4. 测试用例设计 + 自动化实现
黑盒断言在 `ConfigFileTest.java` 的 `testBlackBox()`：
- `getType()` 为 `null`；不存在应用名返回 `null`；`refresh()` 不抛。
- 用临时 JAAS 配置文件 `ConfigFile(URI)`：已知应用名返回 1 条 entry，校验 module 名、`REQUIRED` 标志与 `debug=true` 选项；刷新后仍可解析；用完清理临时文件。

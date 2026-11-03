# ConfigurationSpi 设计案（行为化测试）

## 1. API 功能
`javax.security.auth.login.ConfigurationSpi` 是 `Configuration` 的 SPI 抽象基类：
- 公开无参构造器（供 Provider 子类调用）；
- `protected abstract AppConfigurationEntry[] engineGetAppConfigurationEntry(String name)`：按应用名返回条目，未命中返回 null；
- `protected void engineRefresh()`：默认 no-op，可覆盖实现重载。

`Configuration.getInstance` 通过 `ConfigDelegate` 把 `getAppConfigurationEntry` / `refresh` 委托给对应 `ConfigurationSpi` 实现（源码 `~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/login/ConfigurationSpi.java` 与 `Configuration.java`）。

## 2. Java 用户如何使用
第三方 Provider 提供配置实现：
```java
public class MyConfigSpi extends ConfigurationSpi {
    protected AppConfigurationEntry[] engineGetAppConfigurationEntry(String name) {
        return "app".equals(name) ? entries : null;
    }
    protected void engineRefresh() { reload(); }
}
```
普通用户不直接使用，而是经 `Configuration.getInstance(...)` 获得委托实例。

## 3. 开源代码实际用法调研
- JDK 默认 Provider 无 `ConfigurationSpi` 实现（默认走 `sun.security.provider.ConfigFile`）；标准类型 "JavaLoginConfig" 经 `jdk.security.auth` 模块的 `com.sun.security.auth.login.ConfigFile` 提供（`~/git/jdk25u-dev/src/jdk.security.auth/share/classes/com/sun/security/auth/login/ConfigFile.java`）。
- JDK 测试 `~/git/jdk25u-dev/test/jdk/javax/security/auth/login/Configuration/GetInstanceConfigSpi.java` 自定义 `ConfigurationSpi` 子类挂到自定义 Provider 上验证 `getInstance` 委托。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`ConfigurationSpiTest.java` 的 `testBlackBox()`）：
- 子类 `instanceof ConfigurationSpi`；
- `engineGetAppConfigurationEntry("app")` 返回条目（模块名/flag 正确）、未知名返回 null、名字透传；
- 覆盖 `engineRefresh` 后调用计数；不覆盖时默认 no-op 可调用不抛异常。
白盒（`testWhiteBox()`）：public 抽象类、公开构造器、两个 engine 方法存在且为 protected。

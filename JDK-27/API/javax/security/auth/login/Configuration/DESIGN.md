# Configuration 设计案（行为化测试）

## 1. API 功能
`javax.security.auth.login.Configuration` 是抽象类，负责按应用名返回 LoginModule 配置条目：
- `static getConfiguration()`：返回已安装的 Configuration；无则按 `login.configuration.provider` 安全属性装入默认实现（`sun.security.provider.ConfigFile`）。
- `static setConfiguration(Configuration)`：全局安装（单一运行时实例）。
- `static getInstance(String type, Parameters, [provider])`：按标准类型/Provider 工厂化，返回委托给 `ConfigurationSpi` 的 `ConfigDelegate`。
- 实例方法：抽象 `getAppConfigurationEntry(String)`；`getProvider()` / `getType()` / `getParameters()` 默认返回 null；`refresh()` 默认 no-op。

## 2. Java 用户如何使用
程序化配置（不依赖 `-Djava.security.auth.login.config`）：
```java
Configuration old = Configuration.getConfiguration();
Configuration.setConfiguration(new Configuration() {
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        return "app".equals(name) ? entries : null;
    }
});
try { new LoginContext("app").login(); }
finally { Configuration.setConfiguration(old); }
```

## 3. 开源代码实际用法调研
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/login/Configuration.java`：`getConfiguration()` 惰性装入 `sun.security.provider.ConfigFile`，`getInstance` 经 `GetInstance` 工厂化。
- JDK 测试 `~/git/jdk25u-dev/test/jdk/javax/security/auth/login/Configuration/GetInstance.java` 验证 `Configuration.getInstance("JavaLoginConfig", null)` 与未知类型抛 `NoSuchAlgorithmException`；`.../LoginContext/DynamicConfigurationTest.java` 用自定义 Configuration 驱动登录。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`ConfigurationTest.java` 的 `testBlackBox()`）：
- 默认 `getConfiguration()` 非 null，且 `getParameters/getType/getProvider` 为 null、`refresh()` 可调用；
- `setConfiguration`/`getConfiguration` 往返（自定义子类），`getAppConfigurationEntry` 命中/未命中；
- 未知类型 `getInstance("JckNoSuchConfigType", null)` 抛 `NoSuchAlgorithmException`；
- finally 恢复原 Configuration（隔离全局状态）。
白盒（`testWhiteBox()`）：public 抽象类、直接父类 Object、无 public 构造器、核心方法存在。

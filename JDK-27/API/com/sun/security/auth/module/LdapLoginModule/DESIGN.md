# com.sun.security.auth.module.LdapLoginModule 测试设计案

## 1. API 功能
`com.sun.security.auth.module.LdapLoginModule` 实现 `javax.security.auth.spi.LoginModule`。JDK-27 javap 实测公开方法：
- 构造器 `LdapLoginModule()`。
- `void initialize(Subject, CallbackHandler, Map<String,?>, Map<String,?>)`。
- `boolean login() / commit() / abort() / logout()`（各自 throws `LoginException`）。

## 2. Java 用户如何使用
正常 JAAS 生命周期由 `LoginContext` 驱动：`initialize → login → commit → logout`。本测试只走到 `initialize`：
```java
LdapLoginModule mod = new LdapLoginModule();
mod.initialize(subject, null, sharedState, options);
```
真实 `login()/commit()` 需要 真实 LDAP 服务器与绑定认证，无法在无外部依赖的 headless 测试中运行。

## 3. 开源代码实际用法调研
- 实现基准：`~/git/jdk25u-dev/src/jdk.security.auth/share/classes/com/sun/security/auth/module/LdapLoginModule.java`。
- JDK 回归：`~/git/jdk25u-dev/test/jdk/com/sun/security/auth/module/` 下相关用例（如 `AllPlatforms.java`、`KeyStoreLoginModule/OptionTest.java`、`LdapLoginModule/CheckConfigs.java`）。
（网络不可用，以 JDK 源码 + test/jdk 为权威来源。）

## 4. 测试用例设计 + 自动化实现
白盒断言在 `testWhiteBox()`：类非接口、实现 `LoginModule`、存在 `initialize/login/commit/abort/logout`。
黑盒断言在 `testBlackBox()`（**降级为只 initialize、不真 login/commit**）：
- 构造不抛；`initialize(subject, null, 空 sharedState, 空 options)` 不抛。
- `initialize` 不修改调用方传入的 `sharedState/options`。
- 换一个 `Subject` 再次 `initialize` 不抛。

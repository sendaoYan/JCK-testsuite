# LoginContext 设计案（行为化测试）

## 1. API 功能
`javax.security.auth.login.LoginContext` 是 JAAS 认证入口，按应用名查 Configuration、实例化 LoginModule、执行两阶段认证（login/commit，失败则 abort）：
- 构造器：`(String name)`、`(name, Subject)`、`(name, CallbackHandler)`、`(name, Subject, CallbackHandler)`、`(name, Subject, CallbackHandler, Configuration)`；
- `login()`：执行认证，成功则 subject 装入 principal/credential；
- `logout()`：对每个模块调用 logout；subject 为 null（未登录）时抛 LoginException；
- `getSubject()`：未提供 subject 且未成功登录时返回 null，否则返回 subject。

源码 `~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/login/LoginContext.java`：构造时若名字与 "other" 均无配置条目立即抛 LoginException；模块类找不到时 login() 抛 LoginException("No LoginModule found for ...")。

## 2. Java 用户如何使用
```java
LoginContext lc = new LoginContext("app");       // 名字须在 Configuration 中
lc.login();                                       // 两阶段认证
Subject s = lc.getSubject();                      // 认证后的 subject
Subject.doAs(s, (PrivilegedAction<Void>) () -> { work(); return null; });
lc.logout();
```
无配置时用 `new LoginContext(name, subject, handler, customConfig)` 或程序化 `Configuration.setConfiguration(...)`。

## 3. 开源代码实际用法调研
- JDK 测试 `~/git/jdk25u-dev/test/jdk/javax/security/auth/login/LoginContext/DynamicConfigurationTest.java`：自定义 Configuration + LoginModule，验证错误配置名构造抛 LoginException、正确密码登录、错误密码 login() 抛 LoginException、flag 组合语义。
- JDK 测试 `.../LoginContext/LCTest.java`、`.../LoginContext/ModuleSubject.java`：验证 subject 传播与 doAs。
- JDK 测试 `.../LoginContext/StandardCallbacks.java`、`.../Configuration/GetInstance.java`：验证回调处理器与 provider 化 Configuration。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`LoginContextTest.java` 的 `testBlackBox()`）：
- 无配置名：构造即抛 LoginException（精确时机=构造期）；
- 自定义 REQUIRED LoginModule 登录成功：getSubject 非空、principal/credential 由 commit 塞入、login/commit 被调用、abort 未调用；
- login 前 getSubject 返回 null；logout 前抛 LoginException；
- logout 调用模块 logout、清空 subject principal/credential、getSubject 仍返回 subject；
- 连续两次 login 保持同一 subject；
- `Subject.doAs` 传播登录 subject；
- 模块类不存在时 login() 抛 LoginException；失败/未登录 getSubject 返回 null；
- finally 恢复原全局 Configuration。
白盒（`testWhiteBox()`）：public 具体类、login/logout/getSubject 方法、五个构造器存在。

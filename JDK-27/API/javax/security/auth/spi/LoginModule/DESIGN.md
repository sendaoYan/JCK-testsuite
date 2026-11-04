# LoginModule 设计案

## 1. API 功能
`javax.security.auth.spi.LoginModule` 是认证技术提供者的 SPI 接口，`LoginContext` 据此加载并驱动模块。
五个方法：
- `initialize(Subject, CallbackHandler, Map<String,?> sharedState, Map<String,?> options)`：初始化。
- `boolean login()`：阶段一认证（可抛 `LoginException`）。
- `boolean commit()`：阶段二提交（可抛 `LoginException`）。
- `boolean abort()`：阶段二回滚（可抛 `LoginException`）。
- `boolean logout()`：登出（可抛 `LoginException`）。

来源：`~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/spi/LoginModule.java`。

## 2. Java 用户如何使用
```java
LoginModule m = new MyLoginModule();
m.initialize(subject, callbackHandler, sharedState, options);
boolean ok = m.login();
if (overallSucceeded) { m.commit(); } else { m.abort(); }
m.logout();
```
典型链路：`initialize → login → commit（成功）/abort（失败）→ logout`。

## 3. 开源代码实际用法调研
- JDK 源码：接口 5 方法全部抽象；`initialize` 的两张 `Map` 为 `Map<String,?>`；`login/commit/abort/logout`
  均声明 `throws LoginException`；实现类必须有无参构造器。
- JDK 自带测试 `test/jdk/javax/security/auth/spi/`（`FirstLoginModule.java`、`SecondLoginModule.java`、`Loader.java`、
  `sl.conf`）：实现模块通过 `LoginContext` 加载并依次调用这些方法（本测试调研了该目录）。
- `commit/abort/logout` 返回 boolean（成功/忽略），`login` 返回 boolean（成功/忽略）。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`LoginModuleTest.java` 的 `testBlackBox()`）：
- 匿名实现记录调用顺序，`initialize` 接收传入的 subject/callbackHandler（null）/sharedState/options。
- 返回值：`login` true、`commit` true、`abort` false、`logout` true。
- 调用顺序：initialize → login → commit → abort → logout（共 5 次调用）。
- 异常路径：`login()` 抛 `LoginException` 被正确传播。
白盒保留原结构断言：`public` 接口、无父类、含 5 个方法。

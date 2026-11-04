# AccountExpiredException 设计案（行为化测试）

## 1. API 功能
`javax.security.auth.login.AccountExpiredException` 是 JAAS 登录异常体系中的一个异常类，仅声明两个构造器：
- `AccountExpiredException()`：无参，detailMessage 为 null；
- `AccountExpiredException(String msg)`：携带 detailMessage。

`getMessage()`、`initCause()`、`getCause()`、`getLocalizedMessage()` 等全部继承自 `java.lang.Throwable`，本类自身不声明任何新方法。

继承链：AccountExpiredException → AccountException → LoginException → GeneralSecurityException → Exception → Throwable。

## 2. Java 用户如何使用
JAAS `LoginModule` 在认证失败时抛出该语义化异常，`LoginContext.login()` 把它传播给调用方：
```java
try { lc.login(); }
catch (AccountExpiredException e) { e.getMessage(); e.getCause(); }
```
也用于直接构造测试样例，或用无参构造 + `initCause(Throwable)` 建立因果链。

## 3. 开源代码实际用法调研
- JDK 源码定义：`~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/login/AccountExpiredException.java`（本类仅为带 serialVersionUID 的语义标记，无额外成员）。
- JDK 自带 JAAS 测试 `~/git/jdk25u-dev/test/jdk/javax/security/auth/login/`（LoginContext / modules）中，自定义 LoginModule 以异常区分账号锁定/过期/凭证缺失等失败原因，本类作为 API 契约供第三方 LoginModule 抛出。

## 4. 测试用例设计 + 自动化实现
黑盒断言（见 `AccountExpiredExceptionTest.java` 的 `testBlackBox()`）：
- 消息构造：`new AccountExpiredException("hello").getMessage()` 等于 `"hello"`；
- 无参构造 `getMessage()` 为 null；
- 继承链 instanceof：
- `javax.security.auth.login.AccountExpiredException`；
- `javax.security.auth.login.AccountException`；
- `javax.security.auth.login.LoginException`；
- `java.security.GeneralSecurityException`；
- 因果链：`initCause` 返回自身、`getCause` 回读、第二次 `initCause` 抛 `IllegalStateException`。

白盒（`testWhiteBox()`）：public 具体类、直接父类名、无参与 String 两个构造器存在。

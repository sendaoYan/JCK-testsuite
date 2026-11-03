# java.security.AuthProvider 黑盒行为测试设计

## 1. API 功能
- 继承 `Provider`；抽象方法 `login(Subject, CallbackHandler)`、`logout()`、`setCallbackHandler(CallbackHandler)`

## 2. Java 用户如何使用
登录型安全提供者（如 PKCS#11）实现；用户 `Provider.setName/getServices` 之外还要 `login/logout`。

## 3. 开源代码实际用法调研
- JDK 源码 `/data/yansendao/git/jdk27/src/java.base/share/classes/java/security/AuthProvider.java`（deprecated for removal）
- JDK 测试 `/data/yansendao/git/jdk25u-dev/test/jdk/sun/security/pkcs11/Provider/Login.java`、`LoginISE.java`（真实 AuthProvider login/logout 行为）

## 4. 测试用例设计 + 自动化实现
- 白盒：abstract、构造器 protected、三个抽象方法签名
- 黑盒：匿名子类实现三方法，验证 getName/getInfo/getVersionStr 继承、login 抛出的 LoginException 原样透传、logout/handler 状态可观测

断言入口：`AuthProviderTest.java` 的 `testBlackBox()`（白盒结构断言在同文件 `testWhiteBox()`，保留原有）。

# AppConfigurationEntry.LoginModuleControlFlag 设计案（行为化测试）

## 1. API 功能
`javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag` 是控制 LoginModule 认证流程语义的标记类（不是 enum），公开四个静态常量：
- `REQUIRED` / `REQUISITE` / `SUFFICIENT` / `OPTIONAL`；
- `toString()` 返回 `"LoginModuleControlFlag: " + flag`（flag 为 required/requisite/sufficient/optional，资源前缀见 `~/git/jdk25u-dev/src/java.base/share/classes/sun/security/util/resources/security.properties`）。

构造器私有（源码 `~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/login/AppConfigurationEntry.java`），因此实例只能来自这四个常量；没有 `values()`/`valueOf()`。

## 2. Java 用户如何使用
在 `AppConfigurationEntry` 构造时指定控制标志：
```java
new AppConfigurationEntry("com.example.Mod", LoginModuleControlFlag.REQUIRED, opts);
```
判断：`if (entry.getControlFlag() == LoginModuleControlFlag.SUFFICIENT) ...`，日志用 `flag.toString()`。

## 3. 开源代码实际用法调研
- JDK `LoginContext` 源码（`~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/login/LoginContext.java`）在 `invoke()` 中比较 `entry.getControlFlag() == AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT` 决定提前返回语义。
- JDK 测试 `~/git/jdk25u-dev/test/jdk/javax/security/auth/login/LoginContext/*.java` 通过不同 flag 组合（required/optional/sufficient）验证整体认证成败。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`LoginModuleControlFlagTest.java` 的 `testBlackBox()`）：
- 四个常量非空且两两不同（身份唯一）；
- 字段引用稳定（常量语义）；
- 四个 `toString()` 分别包含 required/requisite/sufficient/optional，且以 `LoginModuleControlFlag` 为前缀。
白盒（`testWhiteBox()`）：public 具体类、非 enum、直接父类 Object、`toString()` 存在、构造器私有。

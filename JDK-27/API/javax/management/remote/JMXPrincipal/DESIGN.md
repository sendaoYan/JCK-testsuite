# JMXPrincipal 测试设计

## 1. API 功能
`javax.management.remote.JMXPrincipal`（实现 `java.security.Principal`、`Serializable`）表示 JMX Remote API 客户端身份。

- 构造器：`JMXPrincipal(String name)`；null 抛 `NullPointerException`。
- 查询：`getName()`。
- 转换/工具：`toString()` 返回 `JMXPrincipal:  <name>`（冒号后两个空格）；`equals()` 仅对 `JMXPrincipal` 且 `name` 相等（大小写敏感）为真；`hashCode()` 为 `name.hashCode()`。

## 2. Java 用户如何使用
```java
JMXPrincipal p = new JMXPrincipal("user");
p.getName();  // "user"
p.equals(new JMXPrincipal("user")); // true
```
典型组合链路：`JMXAuthenticator.authenticate(credentials)` 返回 `Subject`，其中可包含 `JMXPrincipal`；授权/审计时遍历 `Subject.getPrincipals()` 用 `equals/hashCode` 判定身份。

## 3. 开源/源码实际用法调研
- `test/jdk/javax/management/remote/mandatory/RemovedSubjectDelegation.java`、`test/jdk/javax/management/remote/mandatory/passwordAuthenticator/SimpleStandard.java`、`test/jdk/javax/management/remote/mandatory/connection/ConnectionTest.java`：在构造 `Subject` 时 `add(new JMXPrincipal(...))` 作为认证身份。
- JDK 实现 `src/java.management/share/classes/javax/management/remote/JMXPrincipal.java`：`toString()` 为 `"JMXPrincipal:  " + name`，`validate(null)` 抛 `illegal null input`。
来源均为 JDK 仓库（jdk25u-dev）源码与 `test/jdk`。

## 4. 测试用例设计
黑盒部分（`JMXPrincipalTest.testBlackBox()`）：
- 正常路径：`getName`、`toString` 精确值、`equals/hashCode` 一致。
- 组合/边界：空字符串名合法；大小写敏感不等；与 String/null 不等；自反性。
- 异常（精确 message）：null name → `illegal null input`（NPE）。

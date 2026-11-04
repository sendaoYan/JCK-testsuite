# com.sun.security.jgss.InquireSecContextPermission 测试设计

## 1. API 功能
- 类型：`public final class InquireSecContextPermission extends java.security.BasicPermission`（模块 `jdk.security.jgss`，`@Deprecated(since="25", forRemoval=true)`，因 SecurityManager 已不支持）。
- 构造器：`InquireSecContextPermission(String name)`——name 为被允许的 `InquireType` 符号名，`"*"` 表示全部；null 抛 `NullPointerException`、空串抛 `IllegalArgumentException`（继承自 BasicPermission）。
- 继承行为：`getName()`、`getActions()`（恒为空串）、`implies(Permission)`、`newPermissionCollection()`、序列化（`serialVersionUID = -7131173349668647297L`）。

## 2. Java 用户如何使用
```java
import com.sun.security.jgss.InquireSecContextPermission;
InquireSecContextPermission p = new InquireSecContextPermission("KRB5_GET_SESSION_KEY");
boolean ok = p.implies(p);
String actions = p.getActions(); // ""
java.security.PermissionCollection pc = p.newPermissionCollection();
```

## 3. 开源代码实际用法调研
- JDK 源码定义：`~/git/jdk25u-dev/src/jdk.security.jgss/share/classes/com/sun/security/jgss/InquireSecContextPermission.java`，构造器仅 `super(name)`，`@Deprecated(since="25", forRemoval=true)`。
- JDK 自带测试：`~/git/jdk25u-dev/test/jdk/com/sun/security/jgss/InquireSecContextPermissionCheck.java`（`@modules jdk.security.jgss`）验证 `"*".implies(specific)` 为 true、`specific.implies("*")` 为 false、各自 implies 自身为 true。
- 第三方框架不直接使用，行为权威以 JDK 源码 + test/jdk 为准。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`InquireSecContextPermissionTest.testBlackBox()`）覆盖：
- `getName()`、`getActions()==""`。
- `implies` 语义：自身/同名 true，异名 false，`"*"` 蕴含具体权限 true，具体不蕴含 `"*"`。
- `newPermissionCollection()` 非空；加入后 `implies` 生效。
- 序列化 round-trip：`ObjectOutputStream/ObjectInputStream` 反序列化后 `equals` 为 true、name 保持。
- 空名抛 `IllegalArgumentException`、null 抛 `NullPointerException`。
白盒部分补齐：final/extends BasicPermission/标注 Deprecated。

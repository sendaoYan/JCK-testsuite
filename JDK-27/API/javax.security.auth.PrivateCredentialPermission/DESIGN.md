# PrivateCredentialPermission 设计案

## 1. API 功能
`javax.security.auth.PrivateCredentialPermission`（JDK-27 已标记 `@Deprecated(forRemoval=true)`）继承
`java.security.Permission`，保护特定 `Subject` 的私有凭据。目标名语法：
`CredentialClass {PrincipalClass "PrincipalName"}*`；唯一合法动作是 `"read"`（大小写不敏感）。
- 构造器 `PrivateCredentialPermission(String name, String actions)`。
- `getCredentialClass()`、`getPrincipals()`（返回 `String[][]`，每行 `[class, name]`）。
- `implies(Permission)`、`equals`、`hashCode`、`getActions()`（恒 `"read"`）、`newPermissionCollection()`（恒 null）、继承的 `getName()`。

来源：`~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/PrivateCredentialPermission.java`。

## 2. Java 用户如何使用
```java
PrivateCredentialPermission p = new PrivateCredentialPermission(
    "javax.security.auth.x500.X500PrivateCredential javax.security.auth.x500.X500Principal \"CN=Alice\"",
    "read");
p.getCredentialClass();        // 凭据类名
p.getPrincipals();             // String[][] { {"...X500Principal","CN=Alice"} }
p.implies(anotherPermission);  // 通配/子集蕴含
```
典型链路：`构造(名称, "read") → getCredentialClass/getPrincipals → implies/equals → hashCode`。

## 3. 开源代码实际用法调研
- JDK 源码：`init()` 用 `StringTokenizer` 按空格切分，引号包裹的名字可含空格；
  通配 `*` 可用于凭据类与主类名（但 `*` 主类名要求主名也为 `*`）；`implies` 要求本权限主集合是目标主集合的子集。
- JDK 自带测试：
  - `test/jdk/javax/security/auth/PrivateCredentialPermission/Equals.java`：主顺序无关、通配蕴含、equals/implies 对称。
  - `.../CanonError.java`：名字中空格保留（`"pcp 4"` vs `"pcp  4"` 不等）。
  - `.../EmptyName.java`：空名与 null 名抛 `IllegalArgumentException`。
- 上述三条为本测试的权威行为来源，不凭记忆编造。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`PrivateCredentialPermissionTest.java` 的 `testBlackBox()`）：
- 名称解析：`getCredentialClass`/`getActions`/`getPrincipals`（单主、双主、含空格主名保留）。
- 动作：`"READ"` 大小写不敏感且归一化 `"read"`；`"write"` 抛 `IllegalArgumentException`。
- 非法名：空名、null、仅凭据类、未加引号主名 → `IllegalArgumentException`。
- 蕴含：凭据类通配、主集合子集/超集、主名通配、非 PCP（`AllPermission`）为 false。
- 等价/哈希：主顺序无关、同名相等、异主不等、`newPermissionCollection` 为 null、`getName` 返回原始名。
白盒补齐：`public final`、父类 `Permission`、`@Deprecated`。

# com.sun.security.jgss.GSSUtil 测试设计

## 1. API 功能
- 类型：`public class GSSUtil`（模块 `jdk.security.jgss`），GSS-API 与 JDK 实现配合使用的工具类，构造器私有（不可实例化）。
- 唯一公开静态方法：`static javax.security.auth.Subject createSubject(GSSName principals, GSSCredential credentials)`——把 `GSSName` 放入返回 Subject 的 principals 集合、把 `GSSCredential` 的 Kerberos 元素放入 private credentials 集合；参数为 null 时对应集合留空。
- 委托给 `sun.security.jgss.GSSUtil.getSubject(...)`，返回 `new Subject(false, principals, pubCreds, privCreds)`。

## 2. Java 用户如何使用
```java
import javax.security.auth.Subject;
import com.sun.security.jgss.GSSUtil;
Subject s = GSSUtil.createSubject(name, cred);   // name/cred 可为 null
s.getPrincipals();          // 非 null（可为空）
s.getPrivateCredentials();  // 非 null（可为空）
```

## 3. 开源代码实际用法调研
- JDK 源码定义：`~/git/jdk25u-dev/src/jdk.security.jgss/share/classes/com/sun/security/jgss/GSSUtil.java`，`createSubject` 直接调用 `sun.security.jgss.GSSUtil.getSubject(principals, credentials)`。
- JDK 实现：`~/git/jdk25u-dev/src/jdk.security.jgss/share/classes/sun/security/jgss/GSSUtil.java` 中 `getSubject` 在 name/creds 为 null 时返回空集合的 `new Subject(false, emptyPrincipals, emptyPubCreds, emptyPrivCreds)`。
- JDK 自带测试：`~/git/jdk25u-dev/test/jdk/sun/security/krb5/auto/Context.java` 等 krb5 测试使用 `GSSUtil` 相关工具；该 `com.sun` 变体主要用于服务器侧把 GSS 凭据转成 Subject。
- 第三方框架基本不直接用该内部工具类，行为权威以 JDK 源码为准。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`GSSUtilTest.testBlackBox()`）覆盖：
- `createSubject(null, null)` 返回非 null 且 `!isReadOnly()` 的 Subject。
- `getPrincipals()/getPublicCredentials()/getPrivateCredentials()` 均非 null 且为空（null 输入）。
- 两次调用返回两个独立 Subject 且各自为空集合。
白盒部分补齐：具体类、存在 `createSubject` 方法。

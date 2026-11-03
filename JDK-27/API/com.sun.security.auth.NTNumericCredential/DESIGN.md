# com.sun.security.auth.NTNumericCredential 测试设计案

## 1. API 功能
`NTNumericCredential` 抽象 Windows NT 安全令牌（impersonation token），**不实现 `Principal`**（它只是凭据对象）。JDK-27 javap 实测公开 API：
- 构造器 `NTNumericCredential(long token)`：用整数令牌创建。
- `long getToken()`：返回令牌整数。
- `String toString()`：返回含令牌数字的可读字符串。
- `boolean equals(Object)` / `int hashCode()`：按令牌值比较/哈希。

## 2. Java 用户如何使用
```java
NTNumericCredential c = new NTNumericCredential(12345L);
long token = c.getToken();        // 12345
String repr = c.toString();       // 含 "12345"
boolean same = c.equals(new NTNumericCredential(12345L)); // true
```

## 3. 开源代码实际用法调研
- 实现基准：`~/git/jdk25u-dev/src/jdk.security.auth/share/classes/com/sun/security/auth/NTNumericCredential.java`。
- 关联用法：`~/git/jdk25u-dev/src/jdk.security.auth/share/classes/com/sun/security/auth/module/NTLoginModule.java` 在 Windows 登录成功后把 `NTNumericCredential` 加入 `Subject` 的私有凭据集。
（网络不可用，以 JDK 源码为权威来源。）

## 4. 测试用例设计 + 自动化实现
黑盒断言在 `NTNumericCredentialTest.java` 的 `testBlackBox()`：
- 构造 → `getToken()` 返回传入 long；`toString()` 非空且包含令牌数字串。
- `equals`：自反、同值对称、不同值/`null`/其他类型（Long）不等；`hashCode` 一致。
- 负数令牌保留原值；多次访问 `getToken()` 稳定。

# com.sun.security.auth.NTSidPrimaryGroupPrincipal 测试设计案

## 1. API 功能
`com.sun.security.auth.NTSidPrimaryGroupPrincipal` 继承 `com.sun.security.auth.NTSid`，表示 Windows NT 域/组/主组/用户 SID。JDK-27 javap 实测：
- 构造器 `NTSidPrimaryGroupPrincipal(String name)`：调用父类 `NTSid(String)` 校验（`null` 抛 NPE、空串抛 IAE）。
- 继承 `String getName()`；覆盖 `String toString()`（本地化模板含 SID）与 `boolean equals(Object)`（要求 `o instanceof NTSidPrimaryGroupPrincipal` 且 SID 相同）。
- 不覆盖 `hashCode()`，沿用父类 `NTSid.hashCode()`（按 SID）。

## 2. Java 用户如何使用
```java
NTSidPrimaryGroupPrincipal p = new NTSidPrimaryGroupPrincipal("S-1-5-21-1-2-3-500");
String sid = p.getName();                  // 继承自 NTSid
boolean isNTSid = p instanceof com.sun.security.auth.NTSid;
```

## 3. 开源代码实际用法调研
- 实现基准：`~/git/jdk25u-dev/src/jdk.security.auth/share/classes/com/sun/security/auth/NTSidPrimaryGroupPrincipal.java`。
- 关联用法：`NTLoginModule` 登录成功后按域/主组/组分别创建这些 SID 主体加入 `Subject`（见 `module/NTLoginModule.java`）。
（网络不可用，以 JDK 源码为权威来源。）

## 4. 测试用例设计 + 自动化实现
黑盒断言在 `NTSidPrimaryGroupPrincipalTest.java` 的 `testBlackBox()`：
- `instanceof NTSid`；`getName()` 返回 SID；`toString()` 非空且包含 SID。
- `equals` 自反、同 SID 对称、不同 SID 不等；`hashCode` 一致。
- 与同名 SID 的普通 `NTSid` 实例**不**相等（子类 `equals` 要求同子类型）。
- 加入 `Subject` 后可查询。

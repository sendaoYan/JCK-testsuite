# com.sun.security.auth.NTSid 测试设计案

## 1. API 功能
`NTSid` 表示 Windows NT 安全标识符（SID）字符串，实现 `Principal` + `Serializable`。JDK-27 javap 实测：
- 构造器 `NTSid(String stringSid)`：`null` 抛 NPE，空串抛 `IllegalArgumentException`。
- `String getName()`：返回 SID 字符串。
- `String toString()`：返回含 SID 的可读字符串。
- `boolean equals(Object)` / `int hashCode()`：按 SID 字符串比较/哈希。

## 2. Java 用户如何使用
```java
NTSid p = new NTSid("S-1-5-21-1-2-3-500");
String sid = p.getName();  // S-1-5-21-1-2-3-500
```

## 3. 开源代码实际用法调研
- 实现基准：`~/git/jdk25u-dev/src/jdk.security.auth/share/classes/com/sun/security/auth/NTSid.java`。
- 关联用法：`NTSidDomainPrincipal/NTSidGroupPrincipal/NTSidPrimaryGroupPrincipal/NTSidUserPrincipal` 均继承自 `NTSid`（见同名源码）。
（网络不可用，以 JDK 源码为权威来源。）

## 4. 测试用例设计 + 自动化实现
黑盒断言在 `NTSidTest.java` 的 `testBlackBox()`：
- `getName()` 返回 SID；`toString()` 非空且包含 SID。
- `equals`/`hashCode` 一致性与不同 SID、`null`、其他类型的不等。
- `null` 抛 NPE；空串抛 IAE。
- 加入 `Subject` 后可查询。

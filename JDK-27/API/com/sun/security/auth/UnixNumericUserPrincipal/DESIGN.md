# com.sun.security.auth.UnixNumericUserPrincipal 测试设计案

## 1. API 功能
`UnixNumericUserPrincipal` 表示 Unix 用户 ID（UID），实现 `Principal` + `Serializable`。JDK-27 javap 实测：
- 两个构造器：`UnixNumericUserPrincipal(String name)` 与 `UnixNumericUserPrincipal(long name)`（`String` 版 `null` 抛 NPE）。
- `String getName()`：返回 UID 字符串。
- `long longValue()`：解析为 long。
- `String toString()` / `boolean equals(Object)` / `int hashCode()`：按 UID 字符串比较/哈希。

## 2. Java 用户如何使用
```java
UnixNumericUserPrincipal byLong = new UnixNumericUserPrincipal(1000L);
UnixNumericUserPrincipal byStr  = new UnixNumericUserPrincipal("1000");
boolean same = byLong.equals(byStr); // true
```

## 3. 开源代码实际用法调研
- 实现基准：`~/git/jdk25u-dev/src/jdk.security.auth/share/classes/com/sun/security/auth/UnixNumericUserPrincipal.java`。
- 关联用法：`module/UnixLoginModule.java` 用其表示 UID 加入 `Subject`。
（网络不可用，以 JDK 源码为权威来源。）

## 4. 测试用例设计 + 自动化实现
黑盒断言在 `UnixNumericUserPrincipalTest.java` 的 `testBlackBox()`：
- 两种构造器 `longValue()/getName()` 一致且等价。
- `equals`/`hashCode` 一致性、不同 UID/`null`/其他类型不等；`null` 名抛 NPE；加入 `Subject` 后可查询。

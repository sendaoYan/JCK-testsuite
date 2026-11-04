# com.sun.security.auth.UnixNumericGroupPrincipal 测试设计案

## 1. API 功能
`UnixNumericGroupPrincipal` 表示 Unix 组 ID（GID），实现 `Principal` + `Serializable`。JDK-27 javap 实测：
- 两个构造器：`UnixNumericGroupPrincipal(String name, boolean primaryGroup)` 与 `UnixNumericGroupPrincipal(long name, boolean primaryGroup)`（`String` 版 `null` 抛 NPE）。
- `String getName()`：返回 GID 字符串。
- `long longValue()`：把名字解析为 long。
- `boolean isPrimaryGroup()`：是否主组。
- `String toString()` / `boolean equals(Object)` / `int hashCode()`：equals 同时比较 GID 名字与 primaryGroup 标志。

## 2. Java 用户如何使用
```java
UnixNumericGroupPrincipal primary = new UnixNumericGroupPrincipal(1000L, true);
UnixNumericGroupPrincipal viaStr  = new UnixNumericGroupPrincipal("1000", true);
boolean sameGid = primary.equals(viaStr);   // true（两种构造等价）
boolean primaryFlag = primary.isPrimaryGroup(); // true
```

## 3. 开源代码实际用法调研
- 实现基准：`~/git/jdk25u-dev/src/jdk.security.auth/share/classes/com/sun/security/auth/UnixNumericGroupPrincipal.java`。
- 关联用法：`module/UnixLoginModule.java` 用 `UnixNumericGroupPrincipal` 表示主组与补充组加入 `Subject`。
（网络不可用，以 JDK 源码为权威来源。）

## 4. 测试用例设计 + 自动化实现
黑盒断言在 `UnixNumericGroupPrincipalTest.java` 的 `testBlackBox()`：
- 两种构造器 `longValue()/getName()/isPrimaryGroup()` 一致。
- 主/补充组标志参与 `equals`：同 GID 不同 primary 标志不相等；同 GID 同标志的 long/String 构造相等且 `hashCode` 一致。
- `null` 名抛 NPE；加入 `Subject` 后可查询。

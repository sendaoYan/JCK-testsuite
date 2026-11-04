# DelegationPermission 测试设计

## 1. API 功能
`javax.security.auth.kerberos.DelegationPermission` 表示 Kerberos 委派权限（JDK 25 起 `@Deprecated(since="25", forRemoval=true)`），继承 `BasicPermission`，实现 `Serializable`。

- 构造器：`DelegationPermission(String principals)`、`DelegationPermission(String principals, String actions)`（actions 应为 null）。
- 语义：目标名是两个带引号的主体 `"subordinate@R1" "service@R2"`；`implies` 严格等于 `equals`；`equals`/`hashCode` 基于 subordinate+service；`getActions()` 为空串。
- 集合：`newPermissionCollection()`。

## 2. Java 用户如何使用
旧式 SecurityManager 策略文件中授权委派：

```java
DelegationPermission p = new DelegationPermission(
    "\"backup/bar.example.com@EXAMPLE.COM\" \"krbtgt/EXAMPLE.COM@EXAMPLE.COM\"");
boolean implied = p.implies(new DelegationPermission(sameName));
```

## 3. 开源代码实际用法调研
- JDK 自带测试 `DelegationPermissionHash.java`：验证不同数量空白分隔的等价目标名 `equals`/`hashCode`/`implies` 一致。
- JDK 自带测试 `DelegationPermissionInit.java`：列出合法输入（单/双空格）与大量非法输入（少引号、多引号、`-` 分隔、尾部多余内容等），断言非法输入抛 `IllegalArgumentException`。
- JDK 自带测试 `DelegationPermissionCollection.java`：验证 `newPermissionCollection()` 的 add/implies/elements 语义。

来源：JDK-27 对应实现与 `test/jdk`。

## 4. 测试用例设计 + 自动化实现
对应 `DelegationPermissionTest.java` 黑盒部分：
1. 单参/双参构造：getName（原始串）、getActions 为空。
2. `implies` 严格等于 equals（同名额、异 subordinate 不等）。
3. equals/hashCode：不同空白分隔等价、交换 subordinate/service 不等、null/非 Permission 不等。
4. 非法输入（单主体、无引号、`-` 分隔）抛 `IllegalArgumentException`；null 抛 NPE。

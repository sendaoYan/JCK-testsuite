# com.sun.security.auth.UnixPrincipal 测试设计案

## 1. API 功能
`com.sun.security.auth.UnixPrincipal` 是 `java.security.Principal` 实现，同时实现 `java.io.Serializable`。JDK-27 javap 实测公开 API：
- 构造器 `UnixPrincipal(String name)`：用字符串名字创建主体；`null` 抛 `NullPointerException`。
- `String getName()`：返回构造时传入的原始名字。
- `String toString()`：返回包含名字的可读字符串（本地化模板，形如 `类名: 0`）。
- `boolean equals(Object)` / `int hashCode()`：按名字比较/哈希，相等对象哈希一致。

## 2. Java 用户如何使用
典型组合链路：
```java
UnixPrincipal p = new UnixPrincipal("jck-user");
String name = p.getName();                  // 与构造串一致
javax.security.auth.Subject s = new javax.security.auth.Subject();
s.getPrincipals().add(p);                    // 加入 Subject
boolean hit = s.getPrincipals().contains(p); // 集合查询
```


## 3. 开源代码实际用法调研
- 实现基准：`~/git/jdk25u-dev/src/jdk.security.auth/share/classes/com/sun/security/auth/UnixPrincipal.java`（getName/toString/equals/hashCode 源码）。
- JDK 回归：`~/git/jdk25u-dev/test/jdk/com/sun/security/auth/UnixPrincipalHashCode.java` 演示 LoginModule 提交后 `Subject` 携带该主体并打印。
（网络不可用，以 JDK 源码 + test/jdk 为权威来源，未凭记忆编造。）

## 4. 测试用例设计 + 自动化实现
黑盒断言全部在 `UnixPrincipalTest.java` 的 `testBlackBox()`：
- 构造 → `getName()` 等于构造串；`toString()` 非空且包含名字。
- `equals`：自反；同名字两实例对称相等；不同名字 / `null` / 其他类型不等。
- 相等实例 `hashCode` 一致。
- `Subject.getPrincipals().add(p)` 后 `contains(p)` 与 `getPrincipals(UnixPrincipal.class)` 均能命中。


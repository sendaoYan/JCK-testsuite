# ServicePermission 测试设计

## 1. API 功能
`javax.security.auth.kerberos.ServicePermission` 表示 Kerberos 服务权限（JDK 25 起 `@Deprecated(since="25", forRemoval=true)`），继承 `Permission`，实现 `Serializable`。

- 构造器：`ServicePermission(String servicePrincipal, String action)`。
- 动作：`initiate`（发起上下文）、`accept`（接受上下文），可组合；`-` 表示空动作。
- 语义：`implies` = 动作掩码子集 && 名称匹配（`*` 通配任意、`@REALM` 后缀通配）；`getActions()` 返回规范化顺序 `initiate,accept`；`equals/hashCode` 基于 name+mask。
- 集合：`newPermissionCollection()`。

## 2. Java 用户如何使用
旧式 SecurityManager 策略授权：

```java
ServicePermission p = new ServicePermission("krbtgt/EXAMPLE.COM@EXAMPLE.COM", "initiate,accept");
boolean canInitiate = p.implies(new ServicePermission("krbtgt/EXAMPLE.COM@EXAMPLE.COM", "initiate"));
String canonical = p.getActions(); // "initiate,accept"
```

## 3. 开源代码实际用法调研
- JDK 源码自用（`~/git/jdk25u-dev/src/java.security.jgss/.../kerberos/ServicePermission.java`）：`getActions(int mask)` 固定输出 `initiate,accept` 顺序；`impliesIgnoreMask` 处理 `*` 与 `@REALM` 后缀匹配。
- JDK 自带测试 `ServicePermissionTest.java`：验证 null action 抛 NPE。
- JDK 自带测试 `ServicePermissionEquals.java`：验证 `initiate`/`accept`/`initiate,accept` 三者两两不相等且 hashCode 不同。

来源：JDK-27 对应实现与 `test/jdk`。

## 4. 测试用例设计 + 自动化实现
对应 `ServicePermissionTest.java` 黑盒部分：
1. initiate/accept/组合动作的 getActions 规范化（含 `accept,initiate` → `initiate,accept`）。
2. implies 掩码子集：both 蕴含 initiate/accept/自身；initiate 不蕴含 accept/both。
3. 通配：`*` 名称蕴含任意名称；服务名蕴含其 `@REALM` 后缀通配；不蕴含其他 realm。
4. implies 非 ServicePermission 为 false。
5. equals/hashCode：动作顺序无关、异动作/异名不等。
6. `-` 动作 → 空 actions。
7. 非法参数：null name/action、空 action、未知 action。

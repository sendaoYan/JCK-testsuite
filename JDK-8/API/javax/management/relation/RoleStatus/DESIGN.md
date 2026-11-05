# javax.management.relation.RoleStatus — 设计案（JDK-8）

## 1. API 功能
- 角色访问问题类型的**常量表**（闭域，共 7 个）：

  | 常量 | 值（实测锁定） |
  |---|---|
  | NO_ROLE_WITH_NAME | 1 |
  | ROLE_NOT_READABLE | 2 |
  | ROLE_NOT_WRITABLE | 3 |
  | LESS_THAN_MIN_ROLE_DEGREE | 4 |
  | MORE_THAN_MAX_ROLE_DEGREE | 5 |
  | REF_MBEAN_OF_INCORRECT_CLASS | 6 |
  | REF_MBEAN_NOT_REGISTERED | 7 |

- 静态方法：`isRoleStatus(int)`——值 ∈ [1,7] 为 true，其余（0、负数、>7）false。
- 无参构造器 `RoleStatus()`：可实例化的工具类（JDK-8 侧未标 deprecated）。

## 2. Java 用户典型用法
```java
// RelationService 角色校验链返回状态码，包装为 RoleUnresolved
int status = RoleStatus.ROLE_NOT_READABLE;
if (RoleStatus.isRoleStatus(status)) {
    RoleUnresolved ru = new RoleUnresolved(roleName, null, status);
}
```

## 3. 参考来源
- Oracle javadoc：javax.management.relation.RoleStatus（常量表与 isRoleStatus）。
- OpenJDK 源码 `~/git/jdk8u-dev/.../relation/RoleStatus.java`（常量字面量与 isRoleStatus 的 7 项或运算）。
- 行为以 jdk8u372-b07 二进制 `javap -constants` 实测为准。

## 4. 测试用例设计 + 易错规范细节
- 断言：JDK-8 侧 4 → 57。
- 维度：7 常量逐一 == 精确值（1..7）、两两互异全对矩阵、isRoleStatus 完整矩阵（1..7 true；0/8/9/-1/-2/100/MIN/MAX false）、类形态（具体类、可实例化、**非 Serializable**）、与 RoleUnresolved 的闭环集成（7 常量逐一作为 problemType 被接受、8 被拒）。
- **易错点 1（实测）**：总纲曾按 4 常量猜测（NO_ROLE_WITH_NAME..BAD_ROLE_VALUE）——实际是 **7 常量**且无 BAD_ROLE_VALUE；正确名称含 LESS_THAN_MIN_ROLE_DEGREE/MORE_THAN_MAX_ROLE_DEGREE/REF_MBEAN_OF_INCORRECT_CLASS/REF_MBEAN_NOT_REGISTERED。唯一静态方法是 isRoleStatus（无 isReadable 等布尔映射）。
- **易错点 2**：RoleStatus 本身**不是** Serializable（与 Role/RoleUnresolved 不同），ObjectStreamClass.lookup 返回 null——断言只能落在 isAssignableFrom 上。
- **易错点 3**：常量即 RoleUnresolved.setProblemType 的合法闭域（1..7），二者构成同一状态机；8/0/负数在 RoleUnresolved 侧抛 IAE `Incorrect problem type.`。
- **版本差异（vs JDK-27）**：常量值、isRoleStatus 行为两侧逐字节一致；唯一差异是 JDK-27 侧 `RoleStatus()` 构造器被 `@Deprecated(since="16", forRemoval=true)`（JDK-8 无此标注，不产生告警）。

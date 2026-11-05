# javax.management.relation.RoleStatus — 设计案

## 1. API 功能
- 常量（问题类型码）：`NO_ROLE_WITH_NAME=1`、`ROLE_NOT_READABLE=2`、`ROLE_NOT_WRITABLE=3`、`LESS_THAN_MIN_ROLE_DEGREE=4`、`MORE_THAN_MAX_ROLE_DEGREE=5`、`REF_MBEAN_OF_INCORRECT_CLASS=6`、`REF_MBEAN_NOT_REGISTERED=7`。
- 静态方法：`isRoleStatus(int)` 判断给定值是否为已知问题类型。
- 一个 `@Deprecated` 的无参构造器（不推荐调用）。

## 2. Java 用户如何使用
典型组合链路：
```java
if (RoleStatus.isRoleStatus(code)) {
    RoleUnresolved ru = new RoleUnresolved(name, value, code);
}
```

## 3. 开源代码实际用法调研
- JDK 源码自用：`javax.management.relation.RelationSupport.getRoleInt` 中 `int pbType = RoleStatus.NO_ROLE_WITH_NAME`，`javax.management.relation.RoleUnresolved.setProblemType` 调用 `RoleStatus.isRoleStatus(pbType)`（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation`）。
- 说明：纯常量 + 判断工具类，无独立开源生态用法。

## 4. 测试用例设计
- 白盒：public 类、无参构造器存在、七个常量字段与 `isRoleStatus` 方法存在。
- 黑盒：七个常量精确值（1..7）、`isRoleStatus` 对 0/1..7/8/-1 的判断。

## 深化（2026-11 批 J）
- 断言：15 → 56。新增：7 常量两两互异全对矩阵、isRoleStatus 扩展边界（-2/9/Integer.MIN_VALUE/Integer.MAX_VALUE 均 false）、类形态（具体类可实例化；**非 Serializable**——ObjectStreamClass.lookup 返回 null）、与 RoleUnresolved 的闭环集成（7 常量逐一作为 problemType 被接受、8 抛 IAE——常量表即 RoleUnresolved 问题类型闭域）。
- 常量真值表（27 侧 javap -constants 实测，与 JDK-8 完全一致）：NO_ROLE_WITH_NAME=1、ROLE_NOT_READABLE=2、ROLE_NOT_WRITABLE=3、LESS_THAN_MIN_ROLE_DEGREE=4、MORE_THAN_MAX_ROLE_DEGREE=5、REF_MBEAN_OF_INCORRECT_CLASS=6、REF_MBEAN_NOT_REGISTERED=7；isRoleStatus 仅对 [1,7] 为 true。唯一静态方法 isRoleStatus（总纲猜测的 isReadable 等布尔映射不存在）。
- 版本差异：`RoleStatus()` 构造器在 27 侧被 `@Deprecated(since="16", forRemoval=true)`（编译告警，测试用 @SuppressWarnings 压制），JDK-8 侧无此标注；常量与判定行为两侧逐字节一致。

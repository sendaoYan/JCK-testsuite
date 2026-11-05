# javax.management.relation.RoleResult — 设计案（JDK-8）

## 1. API 功能
- 构造器：`RoleResult(RoleList list, RoleUnresolvedList unresolvedList)`——委托 setRoles/setRolesUnresolved，**null 参数合法**（对应字段保持 null）。
- 查询：`getRoles()`（成功访问的角色）、`getRolesUnresolved()`（失败的角色及原因）。
- 更新：`setRoles(RoleList)`、`setRolesUnresolved(RoleUnresolvedList)`——null 显式清空；非 null 时**深度拷贝**：新建列表并逐元素 `clone()`。

## 2. Java 用户典型用法
```java
RoleResult rr = relationService.getRoles(relationId, Arrays.asList("owner", "member"));
rr.getRoles();               // RoleList：成功的角色
rr.getRolesUnresolved();     // RoleUnresolvedList：失败角色 + RoleStatus 原因
rr.getRoles().size() + rr.getRolesUnresolved().size();  // 分类计数
```

## 3. 参考来源
- Oracle javadoc：javax.management.relation.RoleResult（构造/setter 的拷贝语义）。
- OpenJDK 源码 `~/git/jdk8u-dev/.../relation/RoleResult.java`（setRoles/setRolesUnresolved 逐元素 `(Role)(currRole.clone())` 的深度拷贝实现、null 分支置 null）。
- 行为以 jdk8u372-b07 二进制实测为准。

## 4. 测试用例设计 + 易错规范细节
- 断言：JDK-8 侧 4 → 29。
- 维度：null 构造三形态（(null,null)/(roles,null)/(null,unres)）、构造深度拷贝（列表实例独立 + **元素级克隆**——改源 Role 名后结果不受影响）、setters（替换引用/防御拷贝/null 清空）、组合计数、Serializable 白盒、序列化 round-trip + SUID。
- **易错点 1（实测）**：深度拷贝是**元素级**的——`getRoles().get(0) != 传入的 Role`；对源 Role 改名/setRoleValue、对源列表 clear，均不影响 RoleResult 内部状态。这是与浅拷贝值对象的关键区别。
- **易错点 2**：构造/setter 的 null 是**合法清空**（字段=null），与 Role/RoleInfo 家族的"null 即抛 IAE"语义相反——RoleResult 是结果聚合器，不校验。
- **易错点 3**：getter 返回内部引用（后续 setRoles 替换前的同一实例），无防御拷贝。
- 序列化：serialVersionUID = -6304063118040985512L。
- **版本差异（vs JDK-27）**：JDK-8 有双 serial form（jmx.serial.form=1.0 旧形态动态 SUID），JDK-27 固定新形态；默认形态 SUID 一致，行为逐字节一致。

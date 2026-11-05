# javax.management.relation.RoleResult — 设计案

## 1. API 功能
- 构造器：`RoleResult(RoleList list, RoleUnresolvedList unresolvedList)`。
- 查询：`getRoles()`（成功访问的角色）、`getRolesUnresolved()`（失败访问的角色）。
- 更新：`setRoles(RoleList)`、`setRolesUnresolved(RoleUnresolvedList)`（二者均对输入做防御性深拷贝；传入 null 清空）。

## 2. Java 用户如何使用
典型组合链路：
```java
RoleResult r = relation.getRoles(new String[]{"r1","r2"});
RoleList ok = r.getRoles();                     // 成功角色
RoleUnresolvedList failed = r.getRolesUnresolved(); // 失败角色
```

## 3. 开源代码实际用法调研
- JDK 源码自用：`javax.management.relation.RelationSupport.getRolesInt`/`setRolesInt` 末尾 `new RoleResult(roleList, roleUnresList)` 聚合结果（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation/RelationSupport.java`）。
- 说明：该类是关系服务批量读写返回值的标准载体，用户侧只读 `getRoles`/`getRolesUnresolved`，无独立开源生态用法。

## 4. 测试用例设计
- 白盒：public 具体类、实现 Serializable、构造器与四访问器存在。
- 黑盒：构造+getter、`setRoles`/`setRolesUnresolved` 防御性深拷贝、null setter 清空、空列表支持。

## 深化（2026-11 批 J）
- 断言：13 → 23。新增：null 构造三形态（(null,null) 双 null / (roles,null) / (null,unres)——null 是合法清空，与值对象家族"null 即抛"相反）、**元素级深拷贝**（`getRoles().get(0) != 源 Role`；对源 Role 改名/对源列表 clear 均不影响内部状态；unresolved 侧同构）、序列化 round-trip（roles+unresolved 内容保真）+ serialVersionUID=-6304063118040985512L。
- 负例异常表：无——构造与 setter 均不校验（null 合法清空）。
- 版本差异：仅序列化形态机制（JDK-8 双形态/JDK-27 固定）；SUID 一致。

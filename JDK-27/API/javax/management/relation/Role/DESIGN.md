# javax.management.relation.Role — 设计案

## 1. API 功能
- 构造器：`Role(String roleName, List<ObjectName> roleValue)`（任一参数为 null 抛 IAE）。
- 查询：`getRoleName()`、`getRoleValue()`（返回内部 ArrayList，构造/set 时防御性拷贝）。
- 更新：`setRoleName(String)`、`setRoleValue(List<ObjectName>)`（null 抛 IAE）。
- 工具：`toString()`、`clone()`（独立拷贝）、静态 `roleValueToString(List<ObjectName>)`（ObjectName 之间用换行 `\n` 分隔）。

## 2. Java 用户如何使用
典型组合链路：
```java
Role r = new Role("container", List.of(new ObjectName("d:type=a")));
String n = r.getRoleName();            // 读角色名
List<ObjectName> v = r.getRoleValue(); // 读角色值
r.setRoleName("newName");              // 改名
r.setRoleValue(List.of(ob));           // 改值
String s = r.toString();               // 序列化展示
Role copy = (Role) r.clone();          // 独立副本
String vs = Role.roleValueToString(v); // 多行展示
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/relation/NonArrayListTest.java`：`new Role("left", leftRoleValues)` 构造关系角色值（来源：`~/git/jdk25u-dev/test/jdk`）。
- JDK `test/jdk/javax/management/relation/RelationTypeTest.java`：`new Role("left", Arrays.asList(new ObjectName[]{timer1}))` 构造 Role 后放入 RoleList。
- JDK 源码自用：`javax.management.relation.RelationSupport` 中 `new Role(roleName, newRoleValue)`、`(Role)(currRole.clone())`（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation/RelationSupport.java`）。
- 说明：Spring/Guava 不直接依赖 JMX relation；真实用法集中在 JDK 自测与 JMX 客户端代码，以 `test/jdk` + JDK 源码为准。

## 4. 测试用例设计
- 白盒：public 具体类、实现 Serializable、关键方法签名存在。
- 黑盒：构造+getter、构造防御性拷贝、setter 组合、toString 精确格式、`roleValueToString` 换行分隔与空列表、`clone()` 独立副本、五处 null 参数的精确 IAE message（`Invalid parameter` / `Invalid parameter.`）。

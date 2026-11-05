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

## 深化（2026-11 批 J）
- 断言：30 → 42。新增：构造边界（空名/空值合法；raw 非 ObjectName 元素与 null 元素**被接受**——构造器不做元素类型校验，泛型擦除实测）、getRoleValue 活引用（透过它增删直接改状态）、setRoleValue/setRoleName 防御性拷贝与空串接受、roleValueToString 保序、equals/hashCode **身份语义**（不覆写，同内容 not equal，hashCode==identityHashCode）、序列化 round-trip（保序/独立/ArrayList 类型）+ serialVersionUID=-279985518429862552L、clone 后原对象清空不影响副本。
- 负例异常表（27 侧实测）：ctor(null,·)/(·,null) → IAE `Invalid parameter`（无句点）；setRoleName/setRoleValue(null) → IAE `Invalid parameter.`（带句点）；roleValueToString(null) → IAE `Invalid parameter`。
- 版本差异：JDK-8 的 Role 支持遗留 `jmx.serial.form=1.0` 双序列化形态（动态 SUID），JDK-27 固定新形态；默认形态 SUID 两侧一致。其余消息/格式/校验逐字节一致。

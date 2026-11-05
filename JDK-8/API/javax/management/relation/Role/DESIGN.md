# javax.management.relation.Role — 设计案（JDK-8）

## 1. API 功能
- 构造器：`Role(String roleName, List<ObjectName> roleValue)`（任一参数为 null 抛 IAE，消息 `Invalid parameter`，无句点）。
- 查询：`getRoleName()`、`getRoleValue()`（返回内部 ArrayList 本体——活引用；构造/set 时做防御性拷贝）。
- 更新：`setRoleName(String)`、`setRoleValue(List<ObjectName>)`（null 抛 IAE，消息 `Invalid parameter.`，带句点）。
- 工具：`toString()`（`role name: X; role value: a, b` 逗号分隔）、`clone()`（经 `new Role(name, list)` 重建，值列表独立）、静态 `roleValueToString(List<ObjectName>)`（换行 `\n` 分隔；空列表 → 空串；null → IAE）。

## 2. Java 用户典型用法
```java
Role r = new Role("container",
        new ArrayList<ObjectName>(Arrays.asList(new ObjectName("d:type=a"))));
r.getRoleName();                  // 读角色名
r.getRoleValue();                 // 读角色值（活引用）
r.setRoleName("newName");         // 改名
r.setRoleValue(Arrays.asList(ob));// 改值（防御性拷贝）
Role copy = (Role) r.clone();     // 独立副本
Role.roleValueToString(value);    // 多行展示
```

## 3. 参考来源
- Oracle javadoc：javax.management.relation.Role（构造 null 校验、clone 语义、roleValueToString 换行分隔契约）。
- OpenJDK 源码 `~/git/jdk8u-dev/jdk/src/share/classes/javax/management/relation/Role.java`（消息字面量、双 serial form、`new ArrayList<>(roleValue)` 拷贝行为）。
- OpenJDK 回归思路：`~/git/jdk/test/jdk/javax/management/relation/*`（Role 值对象在 RelationService 测试中的构造用法，仅借鉴思路）。
- 行为以 jdk8u372-b07 二进制实测为准（本批全部用例先经 probe 实测再落断言）。

## 4. 测试用例设计 + 易错规范细节
- 断言：JDK-8 侧 4 → 43。
- 维度：构造矩阵（null/空名/空值/非 ObjectName 原始列表/null 元素）、活引用 getRoleValue、防御性拷贝（构造与 setRoleValue 双向）、clone 独立性、equals/hashCode、toString/roleValueToString 精确格式与保序、序列化 round-trip + serialVersionUID。
- **易错点 1（实测）**：Role **不做元素类型校验**——泛型擦除后 `new Role("r", 含 String 的 raw List)` 被原样接受，null 元素也接受；类型问题只在 RelationService 使用时暴露。
- **易错点 2（实测）**：Role **不覆写 equals/hashCode**——同内容两个实例 equals 为 false，hashCode 即 identityHashCode；与 MBeanInfo 家族的值语义不同。
- **易错点 3（消息）**：构造器消息 `Invalid parameter`（无句点），setter 消息 `Invalid parameter.`（带句点），roleValueToString(null) 与构造器同款（无句点）。空名/空列表合法。
- **易错点 4**：getRoleValue 返回内部列表本体（可透过它改状态）；clone/setRoleValue/构造才产生新列表。
- 序列化：serialVersionUID = -279985518429862552L；round-trip 保序；反序列化副本独立。
- **版本差异（vs JDK-27）**：JDK-8 支持 `jmx.serial.form=1.0` 旧序列化形态（动态 serialVersionUID）；JDK-27 移除该机制、固定新形态。默认形态 SUID 两侧一致，互为兼容。其余行为（消息/格式/校验）两侧完全一致。

# javax.management.MBeanServerPermission 测试设计

## 1. API 功能
`MBeanServerPermission` 继承 `BasicPermission`，是 JMX 安全权限。构造器：(String name) 与 (String name, String actions)。
核心操作：`getName()`、`getActions()`、`implies(Permission)`、`equals`、`hashCode`；非法名称/动作抛 `IllegalArgumentException`。

## 2. Java 用户如何使用
```java
new MBeanServerPermission("findMBeanServer,createMBeanServer").getName();
// -> "createMBeanServer,findMBeanServer"（canonical 顺序）
```

## 3. 开源代码实际用法调研
来源：JDK 源码 `~/git/jdk25u-dev/src/java.management/share/classes/javax/management/MBeanServerPermission.java`；
`test/jdk/javax/management/security` 相关用例校验权限的 `implies`/`equals` 语义及非法参数异常消息。

## 4. 测试用例设计
- 白盒：public/非接口/非抽象、直接父类 `BasicPermission`、构造器与方法形态。
- 黑盒：名称/动作 canonicalization、`implies` 子集/超集/通配、`equals`/`hashCode`、`newPermissionCollection`（如适用），以及非法参数的精确 `IllegalArgumentException` 消息。
- 确定性：固定权限名称/动作字符串；消息经探针实测后写入。

## 深化（2026-11）：target 矩阵/implies/集合/序列化

### 新增覆盖
1. **target 矩阵**：4 合法 target（createMBeanServer/newMBeanServer/findMBeanServer/releaseMBeanServer）构造成功且 getActions 恒 ""；null name → NPE；空名/未知 target → IAE；2 参构造非 null actions → IAE（null actions 合法）。
2. **implies（mask 语义）**：`createMBeanServer implies newMBeanServer`（create 位覆盖 new），反向 false；"*" implies 全部；组合名 "newMBeanServer,createMBeanServer".getName() 规范化为 "createMBeanServer"（mask 覆盖）；comma 组合 implies 各部分。
3. **equals/hashCode 基于 mask**：组合名与规范名单 equals 且 hashCode 相同（实测）。
4. **PermissionCollection**：非空；add 后 implies 精确（只加 new 时不 implies create）。
5. **序列化 round-trip**。

### 参考来源
- Oracle javadoc: MBeanServerPermission（target 名表与 mask）。

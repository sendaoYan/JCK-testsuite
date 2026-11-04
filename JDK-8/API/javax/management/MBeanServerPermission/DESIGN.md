# javax.management.MBeanServerPermission 契约测试设计（2026-11 新建）

## 1. API 功能
- server 生命周期权限（BasicPermission 子类）：target 掩码 create/new/find/releaseMBeanServer。

## 2. Java 用户典型用法
见 JDK-27 侧同目录设计与下节深化用例（双侧同代码）。

## 3. 参考来源
- Oracle javadoc: javax.management.MBeanServerPermission。
- OpenJDK `test/jdk/javax/management/` 对应目录（思路借鉴，代码自写）。
## 4. 测试用例设计
见下节深化清单。

## 深化（2026-11）：target 矩阵/implies/集合/序列化

### 新增覆盖
1. **target 矩阵**：4 合法 target（createMBeanServer/newMBeanServer/findMBeanServer/releaseMBeanServer）构造成功且 getActions 恒 ""；null name → NPE；空名/未知 target → IAE；2 参构造非 null actions → IAE（null actions 合法）。
2. **implies（mask 语义）**：`createMBeanServer implies newMBeanServer`（create 位覆盖 new），反向 false；"*" implies 全部；组合名 "newMBeanServer,createMBeanServer".getName() 规范化为 "createMBeanServer"（mask 覆盖）；comma 组合 implies 各部分。
3. **equals/hashCode 基于 mask**：组合名与规范名单 equals 且 hashCode 相同（实测）。
4. **PermissionCollection**：非空；add 后 implies 精确（只加 new 时不 implies create）。
5. **序列化 round-trip**。

### 参考来源
- Oracle javadoc: MBeanServerPermission（target 名表与 mask）。

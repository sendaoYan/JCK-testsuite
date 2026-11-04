# javax.management.MBeanPermission 契约测试设计（2026-11 新建）

## 1. API 功能
- 细粒度 MBean 操作权限：目标名 className#member[objectNamePattern] 形态 × 动作集（mask 语义）。

## 2. Java 用户典型用法
见 JDK-27 侧同目录设计与下节深化用例（双侧同代码）。

## 3. 参考来源
- Oracle javadoc: javax.management.MBeanPermission。
- OpenJDK `test/jdk/javax/management/` 对应目录（思路借鉴，代码自写）。
## 4. 测试用例设计
见下节深化清单。

## 深化（2026-11）：动作解析/名称形态/implies/序列化

### 新增覆盖
1. **动作解析**：getActions 保序保大小写；"*"+"*" → "*"；动作大小写敏感（"INVOKE" → IAE）；重复动作原样保留。
2. **equals/hashCode**：跨动作顺序 equals==true（mask 语义）但 **hashCode 不同**（JDK 实现基于动作串——规范瑕疵，双侧一致，已锁定不反向断言）；同名同序 hashCode 稳定。
3. **implies 矩阵**：动作子集/不相交/超集；类名 * 与精确互推方向；无 member/objectName 的形态 implies 带 member[on] 形态；implies(null)==false。
4. **名称形态**：`className#member[objectName]` 字符串构造 round-trip；4 参构造组装名与 actions（取第 4 参而非第 2 参）。
5. **异常矩阵**：null/空 name、null/空 actions、未知动作、大写动作 → 全部 IAE。
6. **序列化 round-trip**（equals+hashCode）。

### 参考来源
- Oracle javadoc: MBeanPermission（目标名语法与 mask 语义）。
- OpenJDK `test/jdk/javax/management/security/MBeanPermissionTest.java`（思路）。

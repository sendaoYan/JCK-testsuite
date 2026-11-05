# javax.management.relation.RoleInfo — 设计案

## 1. API 功能
- 常量：`ROLE_CARDINALITY_INFINITY = -1`（表示无上下限基数）。
- 构造器：7 参全量 `(roleName, mbeanClassName, read, write, min, max, descr)`、4 参（min/max 默认 1、desc 默认 null）、2 参（read/write 默认 true、min/max 1）、拷贝构造器 `(RoleInfo)`。
- 查询：`getName`/`isReadable`/`isWritable`/`getDescription`/`getMinDegree`/`getMaxDegree`/`getRefMBeanClassName`。
- 校验：`checkMinDegree(int)`、`checkMaxDegree(int)`（返回值落在 `[0, +∞)` 且满足 min/max 约束时为 true）。
- 工具：`toString()`。

## 2. Java 用户如何使用
典型组合链路：
```java
RoleInfo ri = new RoleInfo("container", "javax.management.timer.TimerMBean", true, false, 1, 3, "desc");
if (!ri.checkMinDegree(objs.size()) || !ri.checkMaxDegree(objs.size())) { /* 拒绝 */ }
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/relation/RelationTypeTest.java`：`new RoleInfo("left", "javax.management.timer.TimerMBean")` 定义关系类型的角色（来源：`~/git/jdk25u-dev/test/jdk`）。
- JDK `test/jdk/javax/management/relation/NonArrayListTest.java`：`new RoleInfo("left", Timer.class.getName())`。
- JDK 源码自用：`javax.management.relation.RelationService.checkRoleInt` 调用 `roleInfo.checkMinDegree`/`checkMaxDegree` 完成角色基数校验（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation/RelationService.java`）。
- 说明：`mbeanClassName` 自 JMX 1.2 起不再做类加载校验，任何非空字符串均可（源码 `init` 仅赋值，无 `Class.forName`）。

## 4. 测试用例设计
- 白盒：public 具体类、实现 Serializable、`ROLE_CARDINALITY_INFINITY == -1`、四个构造器与全部 getter/校验方法存在。
- 黑盒：三种构造器默认值断言、拷贝构造器独立性、`checkMinDegree`/`checkMaxDegree` 边界（含无穷基数）、`toString` 精确格式、null 参数与 min>max 的精确异常 message。

## 深化（2026-11 批 J）
- 断言：33 → 63。新增：度校验矩阵（min>max 五组与 min<-1/max<-1 三组，逐一精确 IRIE 消息；合法边界 (-1,-1)/(0,0)/(0,-1)/(5,-1)/(4,4) 原样保存；拷贝构造保 -1/-1）、readable=false&&writable=false **合法**（无可访问性校验，实测）、空名/空类名合法、`no.such.Class` 不加载不抛（声明的 ClassNotFoundException 是死签名）、null 校验先于度校验、checkMinDegree/checkMaxDegree 负值边界（有限度时 -1 一律 false；无穷度接受 -1；-2 永远 false；min=0 接受任意非负）、equals/hashCode 身份语义、null description 的 toString 字面 "null"、序列化 round-trip 全 7 字段 + serialVersionUID=2504952983494636987L。
- 负例异常表（27 侧实测）：null 名/类/拷贝源 → IAE `Invalid parameter.`；min>max（含 min=-1 配有限 max）→ InvalidRoleInfoException `Minimum degree {min} is greater than maximum degree {max}`；度 <-1 → InvalidRoleInfoException `Minimum or maximum degree has an illegal value, must be [0, ROLE_CARDINALITY_INFINITY].`。
- 版本差异：仅序列化形态机制（JDK-8 双形态/JDK-27 固定）；校验逻辑与消息两侧一致。

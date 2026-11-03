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

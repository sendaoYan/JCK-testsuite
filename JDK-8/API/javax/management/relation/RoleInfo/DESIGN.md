# javax.management.relation.RoleInfo — 设计案（JDK-8）

## 1. API 功能
- 构造器 4 个：7 参全参 `(name, mbeanClassName, read, write, min, max, descr)`、4 参 `(name, cls, read, write)`（min=max=1、desc=null）、2 参 `(name, cls)`（再加 read=write=true）、拷贝构造 `(RoleInfo)`（逐字段复制）。
- 度约束：常量 `ROLE_CARDINALITY_INFINITY = -1` 表示无上限；`checkMinDegree(int)`/`checkMaxDegree(int)` 校验值域。
- 查询：`getName/isReadable/isWritable/getDescription/getMinDegree/getMaxDegree/getRefMBeanClassName/toString`。
- 声明抛 `ClassNotFoundException`/`NotCompliantMBeanException` 但 **从不抛出**（JMX 1.2 兼容保留，类名从不加载）。

## 2. Java 用户典型用法
```java
RoleInfo info = new RoleInfo("owner", "com.example.OwnerMBean",
        true, true, 0, RoleInfo.ROLE_CARDINALITY_INFINITY, "owns the resource");
info.checkMinDegree(2);           // true：成员数下限校验
info.checkMaxDegree(99);          // true：无上限
RoleInfo copy = new RoleInfo(info); // 独立副本
```

## 3. 参考来源
- Oracle javadoc：javax.management.relation.RoleInfo（degree 约束与默认值、ROLE_CARDINALITY_INFINITY 语义）。
- OpenJDK 源码 `~/git/jdk8u-dev/.../relation/RoleInfo.java`（init() 校验分支顺序与消息字面量、checkMinDegree/checkMaxDegree 精确实现）。
- OpenJDK 回归思路：`~/git/jdk/test/jdk/javax/management/relation/RelationTypeTest.java`（RoleInfo 在关系类型中的构造用法）。
- 行为以 jdk8u372-b07 二进制实测为准。

## 4. 测试用例设计 + 易错规范细节
- 断言：JDK-8 侧 5 → 76。
- 维度：4 构造矩阵（显式字段 + 默认值 + 拷贝构造全 7 字段）、度校验矩阵（min>max / 越界 / 合法边界）、null 矩阵（4 构造逐一）、checkMinDegree/checkMaxDegree 边界（含 -1/-2 负值边）、equals/hashCode 身份语义、toString 精确格式、序列化 round-trip + SUID。
- **易错点 1（实测）**：min>max 抛的是 **InvalidRoleInfoException**（不是 IAE），消息 `Minimum degree {min} is greater than maximum degree {max}`；`min=-1（无穷）与有限 max` 组合也走该分支（消息同款）；`min<-1 或 max<-1` 走第二分支，消息 `Minimum or maximum degree has an illegal value, must be [0, ROLE_CARDINALITY_INFINITY].`。null 校验（IAE `Invalid parameter.`）先于度校验。
- **易错点 2（实测）**：readable=false 且 writable=false **合法**——RoleInfo 无可访问性校验（总纲曾猜测抛 IAE，实测不抛）。
- **易错点 3（实测 check 边界）**：`checkMinDegree(-1)` 在 minDegree=-1 时为 true；有限 min/max 时 -1 一律 false（`checkMaxDegree(-1)` 在 max=3 时也 false，尽管 -1<=3）；-2 永远 false；min=0 接受任意非负值。
- **易错点 4**：RoleInfo 不覆写 equals/hashCode（身份语义）；`""` 名/类名合法；`no.such.Class` 不抛（类不加载）。
- toString：`role info name: X; isReadable: ...; isWritable: ...; description: null; minimum degree: N; maximum degree: M; MBean class: C`（null description 渲染为字面 "null"）。
- 序列化：serialVersionUID = 2504952983494636987L。
- **版本差异（vs JDK-27）**：JDK-8 支持双 serial form（jmx.serial.form=1.0 旧形态 + 新形态动态选择），JDK-27 固定新形态；默认形态 SUID 一致。校验逻辑/消息/常量（-1）两侧逐字节一致。

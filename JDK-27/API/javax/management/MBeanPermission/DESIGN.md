# javax.management.MBeanPermission 测试设计

## 1. API 功能
`MBeanPermission` 继承 `Permission`，是 JMX 安全权限。构造器：(String name, String actions) 与 (String className, String member, ObjectName objectName, String actions)。
核心操作：`getName()`、`getActions()`、`implies(Permission)`、`equals`、`hashCode`；非法名称/动作抛 `IllegalArgumentException`。

## 2. Java 用户如何使用
```java
new MBeanPermission("*", "getAttribute,setAttribute,invoke").implies(
    new MBeanPermission("*", "getAttribute")); // true
```

## 3. 开源代码实际用法调研
来源：JDK 源码 `~/git/jdk25u-dev/src/java.management/share/classes/javax/management/MBeanPermission.java`；
`test/jdk/javax/management/security` 相关用例校验权限的 `implies`/`equals` 语义及非法参数异常消息。

## 4. 测试用例设计
- 白盒：public/非接口/非抽象、直接父类 `Permission`、构造器与方法形态。
- 黑盒：名称/动作 canonicalization、`implies` 子集/超集/通配、`equals`/`hashCode`、`newPermissionCollection`（如适用），以及非法参数的精确 `IllegalArgumentException` 消息。
- 确定性：固定权限名称/动作字符串；消息经探针实测后写入。

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

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

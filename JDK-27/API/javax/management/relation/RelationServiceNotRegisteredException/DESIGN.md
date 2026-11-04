# javax.management.relation.RelationServiceNotRegisteredException — 设计案

## 1. API 功能
- 单一类：标准 JMX 受检异常，无实例方法（除继承自 `Throwable`/`Exception` 的 `getMessage`/`toString`）。
- 构造器：无参构造器 + `(javax.management.relation.RelationServiceNotRegisteredException)(String message)` 消息构造器，`message` 原样透传给父类。
- 继承链：`javax.management.relation.RelationServiceNotRegisteredException extends javax.management.relation.RelationException`（其下再 `extends Exception`）。

## 2. Java 用户如何使用
用户通常用 `throw new RelationServiceNotRegisteredException("no such relation")` 或捕获后 `e.getMessage()` 读取诊断信息；无参构造器仅用于兼容旧代码。典型组合：
`try { relationService.getRole(...); } catch (RelationServiceNotRegisteredException e) { log(e.getMessage()); }`

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/relation/` 系列（如 `RelationTypeTest.java`、`NonArrayListTest.java`）大量 `catch (RelationNotFoundException e)` / 依赖 relation 异常族。
- JDK 源码自用：`javax.management.relation.RelationService` 内部反复 `throw new RelationServiceNotRegisteredException(excMsg)`（见 `~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation`）。
- 说明：此类为纯异常载体，开源项目仅按惯例 `catch`/`throw`，无复杂行为，故行为断言聚焦构造器与消息传递。

## 4. 测试用例设计
- 白盒：public 具体类、非接口、非抽象、父类为 `javax.management.relation.RelationException`、恰 2 个 public 构造器（无参 + String）、`serialVersionUID` 为 long。
- 黑盒：无参构造器 `getMessage()==null`；String 构造器 message 原样保留；继承链 instanceof（`Exception`/`JMException`/`RelationException`）；`toString()` 为 `<fqcn>: <message>`。

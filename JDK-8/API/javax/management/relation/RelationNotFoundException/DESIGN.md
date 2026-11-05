# javax.management.relation.RelationNotFoundException — 设计案

## 1. API 功能
- 标准 JMX relation 受检异常，仅 ()/(String) 两个 public 构造器（javap 双侧确认）。
- 继承链（实测）：`RelationNotFoundException` extends `javax.management.relation.RelationException` extends `javax.management.JMException` extends `Exception`。
- Serializable；serialVersionUID = **-3793951411158559116**（ObjectStreamClass 实测）。

## 2. Java 用户如何使用
`try { relationService.xxx(...); } catch (RelationNotFoundException e) { log(e.getMessage()); }`；
或直接 `throw new RelationNotFoundException("...")`。无参构造器 message 为 null（仅兼容旧代码）。

## 3. 参考来源
- Oracle javadoc：javax.management.relation 包各异常类的异常契约说明（RelationService/RoleInfo 方法 @exception 子句）。
- OpenJDK 源码：`~/git/jdk8u-dev` 与 `~/git/jdk25u-dev` 的 `src/java.management/share/classes/javax/management/relation/`（RelationService/RoleInfo/RelationTypeSupport 的 throw 点与消息文本），只借鉴思路、代码自写。

## 4. 测试用例设计
- 构造器矩阵：() → message null、cause null；(String) → message 原样、cause null。
- 层级：instanceof RelationException/JMException/Exception 三级；Throwable 可抛出。
- 序列化：ObjectStreamClass SUID 数值锁定；round-trip 保留具体类与消息。
- 真实触发点（本地 RelationService 闭环，非直接构造冒烟）：
  真实触发：注册 RelationService 后 removeRelation("bogus") / isRelationMBean("bogus") / getRole("bogus", role) / sendRelationCreationNotification("bogus")，全部精确消息 "No relation associated to relation id bogus"；对照 hasRelation("bogus") 返回 Boolean.FALSE 不抛异常。
- 基础设施：newMBeanServer() + RelationService(true) 注册于 jck.rel:type=RelationService + createRelationType("jck.T", RoleInfo("counterRole", Observed 嵌套类, true,true,1,2))。

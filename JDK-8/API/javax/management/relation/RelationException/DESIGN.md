# javax.management.relation.RelationException — 设计案

## 1. API 功能
- 标准 JMX relation 受检异常，仅 ()/(String) 两个 public 构造器（javap 双侧确认）。
- 继承链（实测）：`RelationException` extends `javax.management.relation.RelationException` extends `javax.management.JMException` extends `Exception`。
- Serializable；serialVersionUID = **5434016005679159613**（ObjectStreamClass 实测）。

## 2. Java 用户如何使用
`try { relationService.xxx(...); } catch (RelationException e) { log(e.getMessage()); }`；
或直接 `throw new RelationException("...")`。无参构造器 message 为 null（仅兼容旧代码）。

## 3. 参考来源
- Oracle javadoc：javax.management.relation 包各异常类的异常契约说明（RelationService/RoleInfo 方法 @exception 子句）。
- OpenJDK 源码：`~/git/jdk8u-dev` 与 `~/git/jdk25u-dev` 的 `src/java.management/share/classes/javax/management/relation/`（RelationService/RoleInfo/RelationTypeSupport 的 throw 点与消息文本），只借鉴思路、代码自写。

## 4. 测试用例设计
- 构造器矩阵：() → message null、cause null；(String) → message 原样、cause null。
- 层级：instanceof RelationException/JMException/Exception 三级；Throwable 可抛出。
- 序列化：ObjectStreamClass SUID 数值锁定；round-trip 保留具体类与消息。
- 真实触发点（本地 RelationService 闭环，非直接构造冒烟）：
  根异常无专属触发点：改用「真实触发 + 多态捕获」——经本地 RelationService 触发 RoleNotFoundException（getRole 未知角色），以 catch (RelationException) 捕获并断言具体类型与消息原样保留；另实测 10 个子类全部 instanceof RelationException/JMException（全家族层级表）。
- 基础设施：newMBeanServer() + RelationService(true) 注册于 jck.rel:type=RelationService + createRelationType("jck.T", RoleInfo("counterRole", Observed 嵌套类, true,true,1,2))。

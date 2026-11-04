# javax.management.relation.RelationNotification — 设计案

## 1. API 功能
- 继承 `Notification`。
- 常量六类通知：`RELATION_BASIC_CREATION`/`RELATION_MBEAN_CREATION`/`RELATION_BASIC_UPDATE`/`RELATION_MBEAN_UPDATE`/`RELATION_BASIC_REMOVAL`/`RELATION_MBEAN_REMOVAL`。
- 两个构造器：创建/删除（带 `unregMBeanList`）、角色更新（带 `name/newValue/oldValue`）。
- 查询：`getRelationId`/`getRelationTypeName`/`getObjectName`/`getMBeansToUnregister`/`getRoleName`/`getOldRoleValue`/`getNewRoleValue`；继承自 Notification 的 `getType`/`getSequenceNumber`/`getTimeStamp`/`getMessage`/`getSource`。

## 2. Java 用户如何使用
典型组合链路：
```java
RelationNotification n = new RelationNotification(RELATION_BASIC_CREATION, src, seq, ts, msg, id, type, obj, unreg);
String rid = n.getRelationId();
List<ObjectName> unreg = n.getMBeansToUnregister();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/relation/RelationNotificationSeqNoTest.java` 与 `RelationNotificationSourceTest.java`：分别验证序列号与 source 透传（来源：`~/git/jdk25u-dev/test/jdk`）。
- JDK 源码自用：`javax.management.relation.RelationService.sendNotificationInt` 构造各类 `RelationNotification` 发送（来源：`~/git/jdk25u-dev/src/java.management/share/classes/javax/management/relation/RelationService.java`）。

## 4. 测试用例设计
- 白盒：public 类、父类 Notification、六个常量精确值、七个 getter 存在。
- 黑盒：创建/删除构造器全 getter（含继承的 type/seq/ts/message/source）、更新构造器的 roleName/new/old 值、`getMBeansToUnregister`/`getNewRoleValue` 防御性拷贝、五处校验失败（错误类型/null source/id/typeName/roleName）的精确 IAE message。

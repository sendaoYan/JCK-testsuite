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

## 深化（2026-11 批 L：构造形态矩阵 + 序列化 + 真闭环）

### 新增覆盖维度
- **6 参构造（creation/removal 形态）**：RELATION_BASIC_REMOVAL / RELATION_MBEAN_CREATION / RELATION_MBEAN_REMOVAL 三种合法类型逐一构造（此前只测过 basic creation）；roleName null、old/new 空列表、objectName null 或精确回读、null unregMBeanList 归一化为空列表。
- **11 参构造（update 形态）**：RELATION_MBEAN_UPDATE 合法构造、空 old 列表归一化、objectName 精确、source 引用在 MBeanServer 之外**不被改写**（getSource()==原对象）。
- **IAE 矩阵（精确消息）**：null source → `"null source"`（来自 Notification 父类）；null id / null typeName / 非法 type / 6 参传 update 类型 / 11 参传 creation 类型 / null roleName / null newValue / null oldValue → `"Invalid parameter."`。
- **序列化**：两种形态 round-trip 全 getter 精确（type/relationId/typeName/objectName/unregister 列表/seq/timeStamp/message/source/roleName/old/new）；`serialVersionUID == -6871117877523310399L`（ObjectStreamClass 锁定）。
- **真闭环**：注册 RelationService(true) + 监听 → createRelation/setRole/removeRelation 收到 real RelationNotification，逐 getter（relationId/typeName/source==服务 ObjectName/roleName/old/new/removal 消息）。

### 易错规范细节（双侧一致）
1. `getMBeansToUnregister()`：非 null 入参返回防御拷贝；**null 入参归一化为不可变 `Collections.emptyList()`**（add → UnsupportedOperationException，测试按记录行为兼容两种实现）。
2. creation/removal 形态的 getRoleName()==null、getOld/NewRoleValue() 为空列表（构造器 safeGet 归一化）。
3. 通知经 MBeanServer 投递后 source 被改写为服务的 ObjectName；不经 server 直接构造则保留原引用。


# javax.management.NotificationEmitter 契约测试设计

## 1. API 功能
- 接口，扩展 NotificationBroadcaster，仅新增一个声明：`removeNotificationListener(NotificationListener, NotificationFilter, Object) throws ListenerNotFoundException`（三参精确移除）。

## 2. Java 用户如何使用
需要按 (listener, filter, handback) 三元组精确移除监听器时使用；NotificationBroadcasterSupport 是参考实现。
```java
emitter.addNotificationListener(l, f, handback);
emitter.removeNotificationListener(l, f, handback);   // 只移除这一注册
```

## 3. 参考来源
- Oracle javadoc：javax.management.NotificationEmitter（三参 remove 的精确匹配语义）。
- 实现权威：`src/share/classes/javax/management/NotificationEmitter.java`、`NotificationBroadcasterSupport.java`（ListenerInfo.equals 按 listener+filter+handback 三元恒等）。
- 双侧 `javap -public` 对照：公开签名一致。

## 4. 测试用例设计 + 易错规范细节
- 白盒：public 接口、父接口 NotificationBroadcaster、恰 1 个声明方法，参数类型/受检异常/void 返回逐项断言。
- 行为（NotificationBroadcasterSupport 实现）：同一 listener 以不同 handback 注册两次 → 每次 send 每注册各投递一次；handback 不匹配的三参 remove → ListenerNotFoundException；精确 (l,f,h) remove 只移除对应注册，另一注册继续投递；再 remove 已删注册 → ListenerNotFoundException；继承的单参 remove 一次清掉剩余全部注册。
- 易错点：三参 remove 按（listener, filter, handback）全等匹配，filter/handback 任一不同即抛 ListenerNotFoundException，而不是"就近删除"。

## 深化（2026-11，批次 d）
- 声明面契约锁定：接口恰声明 1 个方法 removeNotificationListener(NotificationListener, NotificationFilter, Object)，声明恰一个受检异常 ListenerNotFoundException，返回 void。
- 多注册/移除闭环（经 NotificationBroadcasterSupport）：同 listener 双注册（不同 handback）每次 send 各投递一次；mismatched handback 三参 remove → ListenerNotFoundException；精确 (l,f,h) remove 只移除该注册、另一注册继续投递；再 remove 已删注册 → ListenerNotFoundException；继承单参 remove 一次清掉剩余注册；NotificationBroadcasterSupport is-a NotificationEmitter、NotificationEmitter is-a NotificationBroadcaster。

### 版本差异
- 无（双侧 javap 签名与行为一致）。

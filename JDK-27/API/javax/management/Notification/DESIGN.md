# javax.management.Notification 黑盒行为测试设计

## 1. API 功能
- 继承 `java.util.EventObject`。
- 构造：`(type, source, sequenceNumber)`、`(type, source, sequenceNumber, timeStamp)`、`(type, source, sequenceNumber, timeStamp, message)`。
- 查询/设置：get/set Type、SequenceNumber、TimeStamp、Message、UserData、Source。

## 2. Java 用户如何使用
所有 JMX 通知的基类；通知发送方构造后交给 NotificationBroadcasterSupport 分发。
```java
Notification n = new Notification("jmx.attribute.change", src, seq, ts, "changed");
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/notification/NotifInfoTest.java`。
- `test/jdk/javax/management/monitor/*`。
- 实现权威：`src/java.management/share/classes/javax/management/Notification.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：5 参构造返回各字段；3/4 参构造 message 默认为空串 ""（源码确认）；setSequenceNumber/setTimeStamp/setUserData/setSource 生效；toString 含 type。
- 白盒：public 类、父类 EventObject、含 getType/getSequenceNumber/getTimeStamp/getMessage/getUserData 及对应 setter。

## 深化（2026-11，批次 d）
- 构造矩阵补全：负 seq / Long.MIN_VALUE 合法；显式 timeStamp 0 / -1 原样保留；显式 null message（4 参与 5 参构造）保留；3 参与 (type,source,seq,msg) 构造的 timeStamp 由时钟填充（断言落在 [before,after]）；source 按 == 恒等返回（EventObject.getSource 与 Notification.getSource 同一对象）。
- null 语义：null source → IllegalArgumentException（来自 EventObject 构造器）；null type 不抛异常（实现无校验，javadoc 提及 IAE 与实现不符，按实测锁定）；构造后 setSource(null) 合法。
- equals/hashCode：无覆写，恒等语义（同字段两实例不等、自身相等、hashCode 两次一致）；无 compareTo（反射枚举确认）。
- toString 精确锁定：`javax.management.Notification[source=S][type=T][message=M]` 全等。
- 序列化：serialVersionUID = -7516092053498031989L（javadoc 文档值）；负 seq、构造后替换的 source、userData 值 round-trip 全保留。

### 版本差异
- JDK-27（模块强封装）：反射 setAccessible 读私有 serialVersionUID 抛 InaccessibleObjectException（java.management 不 open javax.management）；JDK-8 可读且值等于文档常量。测试按"sealed 分支"两侧各自锁定真实行为（按类名字符串捕获，因该异常类 JDK-8 不存在）。

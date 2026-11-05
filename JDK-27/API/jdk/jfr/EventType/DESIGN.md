# jdk.jfr.EventType 设计案

## 1. API 功能
事件类型元数据：getEventType(Class)/getName/getLabel/getFields/getField/getSettingDescriptors。

## 2. Java 用户如何使用
EventType.getEventType(MyEvent.class).getField("msg")。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/EventType.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 自定义事件类型 name/label/fields/settings。

## 5. 深化（2026-11-05）：契约覆盖扩展
- 注解元数据闭环：@Name/@Label/@Category(2 项有序)/@Description 全等回读；Name 注解元素回读。
- 字段表：4 隐式 [startTime,duration,eventThread,stackTrace] 在前+用户字段按声明序；getField 命中/缺失(null)；setting 派生注解 getAnnotation null。
- setting 默认值表：普通事件恰 3 项 {enabled="true", threshold="0 ns", stackTrace="true"}。
- isEnabled 生命周期：无录制 false→enable+RUNNING true→录制 stop 后回落 false。
- @Registered(false)：不自动注册（getEventTypes 不含）；getEventType(cls)→ISE；显式 register 后出现；unregister 再消失再恢复。
- 注册表成员：jdk.ThreadStart/jdk.NativeMethodSample 双侧；版本差异：jdk.ObjectAllocationSample 27 有 8 无。
- 无 @Category 事件默认类别 ["Uncategorized"]。
负例精确异常表：getEventType 于未注册类→IllegalStateException "Event class ... is not registered"。

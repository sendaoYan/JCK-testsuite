# jdk.jfr.EventType 设计案（JDK-8 (temurin jdk8u372-b07)）

## 1. API 功能
事件类型元数据：name/label/description/categories/fields/setting descriptors/注册状态/isEnabled/id。

## 2. Java 用户如何使用
EventType.getEventType(MyEvent.class) 静态方法获取；FlightRecorder.getEventTypes() 列全部注册类型。

## 3. 参考来源
- ~/git/jdk/test/jdk/jdk/jfr/（官方用例：api/recording、api/event、api/metadata/eventtype、api/metadata/annotationelement、api/metadata/valuedescriptor、api/flightrecorder、api/settings 等）
- ~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/*.java（权威实现）
- jdk.jfr 包 javadoc + 双侧 javap -public 逐方法核对（JDK-8 jdk8u372-b07 / JDK-27 jdk-27.0.1.0.2+1）

## 4. 测试用例设计 + 自动化实现
白盒（反射结构断言）+ 黑盒（真实行为断言）合并在 EventTypeTest.java；static int passed / static void check 骨架，末尾唯一一次 PASS 打印；othervm 独立 JVM；事件名唯一（test.EventType.*）；有界轮询≤2s；临时文件 finally 清理。

### 深化（2026-11-05，契约覆盖）
- 注解元数据闭环：@Name/@Label/@Category(2 项有序)/@Description 全等回读；Name 注解元素回读。
- 字段表：4 隐式 [startTime,duration,eventThread,stackTrace] 在前+用户字段按声明序；getField 命中/缺失(null)；setting 派生注解 getAnnotation null。
- setting 默认值表：普通事件恰 3 项 {enabled="true", threshold="0 ns", stackTrace="true"}。
- isEnabled 生命周期：无录制 false→enable+RUNNING true→录制 stop 后回落 false。
- @Registered(false)：不自动注册（getEventTypes 不含）；getEventType(cls)→ISE；显式 register 后出现；unregister 再消失再恢复。
- 注册表成员：jdk.ThreadStart/jdk.NativeMethodSample 双侧；版本差异：jdk.ObjectAllocationSample 27 有 8 无。
- 无 @Category 事件默认类别 ["Uncategorized"]。
负例精确异常表：getEventType 于未注册类→IllegalStateException "Event class ... is not registered"。

# jdk.jfr.EventFactory 设计案（JDK-8 (temurin jdk8u372-b07)）

## 1. API 功能
动态事件工厂：注解元素+值描述符 → 运行时生成事件类（无需预定义 Java 类）；create 后即可 newEvent/set/commit。

## 2. Java 用户如何使用
EventFactory.create(annotations, fields) → getEventType() 元数据 → register() → newEvent()+set(i,v)+commit() → 录制回读。

## 3. 参考来源
- ~/git/jdk/test/jdk/jdk/jfr/（官方用例：api/recording、api/event、api/metadata/eventtype、api/metadata/annotationelement、api/metadata/valuedescriptor、api/flightrecorder、api/settings 等）
- ~/git/jdk/src/jdk.jfr/share/classes/jdk/jfr/*.java（权威实现）
- jdk.jfr 包 javadoc + 双侧 javap -public 逐方法核对（JDK-8 jdk8u372-b07 / JDK-27 jdk-27.0.1.0.2+1）

## 4. 测试用例设计 + 自动化实现
白盒（反射结构断言）+ 黑盒（真实行为断言）合并在 EventFactoryTest.java；static int passed / static void check 骨架，末尾唯一一次 PASS 打印；othervm 独立 JVM；事件名唯一（test.EventFactory.*）；有界轮询≤2s；临时文件 finally 清理。

### 深化（2026-11-05，契约覆盖）
- 动态事件全链路闭环（旗舰）：Name/Label/Description/Category(String[]) 注解 + 8 类型字段（long/String/int/boolean/double/short/byte/char，各带 Label）→ create 后 EventType 元数据全等（4 隐式+8 用户字段序、label 回读）→ register → 录制 enable → newEvent+set 全部 8 值+commit → dump → RecordedEvent 类型化 getter（getLong/getString/getInt/getBoolean/getDouble/getShort/getByte/getChar）逐字段全等。
- 注册语义（实证修正）：create() 即自动注册（getEventTypes 立即可见）；register() 幂等；unregister 后 getEventTypes 不含且事件被录制静默丢弃（录制内 commit 不抛但文件 0 条——负例闭环）；double unregister 幂等。
- 注解 Category 必须传 String[]（传 String 在 8 侧内部 ClassCastException）。
负例精确异常表（8+27 一致）：create(null 注解,…)→NPE；create(…,null 字段)→NPE；newEvent().set(越界,v)→IndexOutOfBoundsException contains "Index must between 0 and"。

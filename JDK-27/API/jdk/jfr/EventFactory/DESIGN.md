# jdk.jfr.EventFactory 设计案

## 1. API 功能
动态创建事件类型并提交：create/getEventType/newEvent。

## 2. Java 用户如何使用
EventFactory.create(annotations, fields).newEvent().commit()。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/EventFactory.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 create → 类型名 → newEvent → set → commit。

## 5. 深化（2026-11-05）：契约覆盖扩展
- 动态事件全链路闭环（旗舰）：Name/Label/Description/Category(String[]) 注解 + 8 类型字段（long/String/int/boolean/double/short/byte/char，各带 Label）→ create 后 EventType 元数据全等（4 隐式+8 用户字段序、label 回读）→ register → 录制 enable → newEvent+set 全部 8 值+commit → dump → RecordedEvent 类型化 getter（getLong/getString/getInt/getBoolean/getDouble/getShort/getByte/getChar）逐字段全等。
- 注册语义（实证修正）：create() 即自动注册（getEventTypes 立即可见）；register() 幂等；unregister 后 getEventTypes 不含且事件被录制静默丢弃（录制内 commit 不抛但文件 0 条——负例闭环）；double unregister 幂等。
- 注解 Category 必须传 String[]（传 String 在 8 侧内部 ClassCastException）。
负例精确异常表（8+27 一致）：create(null 注解,…)→NPE；create(…,null 字段)→NPE；newEvent().set(越界,v)→IndexOutOfBoundsException contains "Index must between 0 and"。

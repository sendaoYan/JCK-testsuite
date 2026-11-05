# jdk.jfr.consumer.RecordedEvent 设计案

## 1. API 功能
记录的事件（extends RecordedObject）：getEventType/getStartTime/getDuration/getString/hasField/getStackTrace/getThread。

## 2. Java 用户如何使用
RecordingFile.readEvent() 返回，读字段。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedEvent.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 录制自定义事件 → 读 message 字段/时间/线程。

## 5. 深化（本轮）
断言规模：基线 → 41。
### 行为闭环
- 时间语义：end >= start、duration == end - start、getEventType().getName() == @Name 值。
- 字段通道：用户字段 + 隐式字段（eventThread/stackTrace）经 getFields/getValue/hasField 一致可见；hasField("nope")==false。
- withoutStackTrace 事件：stackTrace null 且用户字段完好。
### 实测负例异常表
| 操作 | 异常 | 精确消息 |
|---|---|---|
| `getValue("nope")` | IllegalArgumentException | `Could not find field with name nope` |
### 8/27 差异
面一致；27 注册表更大（ObjectAllocationSample/throttle 等）。

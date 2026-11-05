# jdk.jfr.consumer.RecordedEvent 设计案（JDK-8）

## 1. API 功能
单条录制事件：`getStartTime`/`getEndTime`/`getDuration`/`getEventType` + 继承 RecordedObject 的字段访问。

## 2. Java 用户如何使用
消费端逐事件取时间戳、类型与业务字段。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk8u-dev/jdk/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedEvent.java（8u262 backport）。

## 4. 测试用例设计 + 自动化实现
白盒（结构）+ 黑盒（自定义事件回读）。骨架 `static int passed; check(boolean,String)`。

## 5. 深化（本轮）
断言规模：基线 → 39。
### 行为闭环
- 自定义事件回读：start/end 时间戳有效、end >= start、duration == end - start。
- `getEventType().getName()` 等于 @Name 注解值；用户字段与隐式字段（eventThread/stackTrace）在 getFields 中可见。
- withStackTrace(false) 事件回读后 stackTrace 为 null 且用户字段完好。
- 事件字段通道：getValue(用户字段) 值恒等；hasField("nope") == false。
### 实测负例异常表
| 操作 | 异常 | 精确消息 |
|---|---|---|
| `getValue("nope")`（继承自 RecordedObject） | IllegalArgumentException | `Could not find field with name nope` |
| `getDuration` 声明检查 | （正例：方法存在且返回 end-start） | — |
### 8/27 差异
27 侧事件对象面一致；差异在事件类型注册表内容（如 27 有 ObjectAllocationSample/throttle 设置）。

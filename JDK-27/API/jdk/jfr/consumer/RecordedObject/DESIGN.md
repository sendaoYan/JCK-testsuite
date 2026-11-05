# jdk.jfr.consumer.RecordedObject 设计案

## 1. API 功能
记录对象的基类（extends RecordedObject）：getValue/hasField/getFields。

## 2. Java 用户如何使用
RecordedEvent 是其子类，getValue("field") 取值。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk25u-dev/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedObject.java。

## 4. 测试用例设计 + 自动化实现
白盒（结构断言）+ 黑盒（组合操作）。白盒 关键方法；黑盒 getValue/hasField/getFields/toString。

## 5. 深化（本轮）
断言规模：基线 → 105。
### 行为闭环
- 自定义事件携带全类型字段（byte/char/short/int/long/float/double/boolean/String/Class/Thread/ThreadGroup），回读全 getter 值恒等。
- 拓宽合法（int→long）、收窄非法、隐式字段 hasField 可见、null 值 String 字段返回 null、getValue 多态（Class→RecordedClass、Thread→RecordedThread）。
### 实测负例异常表（类型错配矩阵，消息双侧一致）
| 操作 | 异常 | 精确消息 |
|---|---|---|
| 收窄（getInt(long)、getShort/getByte(int)） | IllegalArgumentException | `Attempt to get field "X" with illegal data type conversion long -> int` |
| 跨类（getXxx(String字段)、getLong(Thread字段) 等） | IllegalArgumentException | `Attempt to get field "X" with illegal data type conversion ...` |
| 不存在字段（getValue/getLong/getBoolean(nope)） | IllegalArgumentException | `Could not find field with name nope` |
| `getString("nope")` | IllegalArgumentException | `Attempt to get unknown field "nope"` |
| `getInstant(无 @Timestamp 的 long)` | IllegalArgumentException | `missing @Timestamp` |
| `getDuration` 非法字段 | IllegalArgumentException | `Attempt to get field ... conversion` 或 not found |
### 8/27 差异
矩阵双侧同构；差异仅事件注册表接受的字段面（Duration 双侧均非法）。

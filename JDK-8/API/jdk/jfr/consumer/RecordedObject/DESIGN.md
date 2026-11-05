# jdk.jfr.consumer.RecordedObject 设计案（JDK-8）

## 1. API 功能
录制对象基类：`getValue(String)`/`hasField`/`getFields` + 全类型化 getter（getByte/getChar/getShort/getInt/getLong/getFloat/getDouble/getBoolean/getString/getClass/getThread/getInstant/getDuration）。

## 2. Java 用户如何使用
RecordedEvent 继承它；消费端按字段名取类型化值。

## 3. 开源代码实际用法调研
- 来源：~/git/jdk8u-dev/jdk/src/jdk.jfr/share/classes/jdk/jfr/consumer/RecordedObject.java（8u262 backport）。
- JDK-8 特性：字段类型系统校验在 `getTypedObject`/原始 getter 内做收拓宽判定。

## 4. 测试用例设计 + 自动化实现
白盒（结构）+ 黑盒（自定义含全类型字段事件 → 回读 → 全 getter 矩阵）。骨架 `static int passed; check(boolean,String)`。

## 5. 深化（本轮）
断言规模：基线 → 97。
### 行为闭环
- 自定义事件携带 byte/char/short/int/long/float/double/boolean/String/Class/Thread/ThreadGroup 字段，回读后逐字段断言值恒等。
- 拓宽合法：`getLong("intField")` 返回拓宽后的 long 值。
- 隐式字段（eventThread、stackTrace）对 `hasField` 可见；`getString` 命中 null 值字段返回 null。
- `getValue` 对 Class 字段返回 RecordedClass、Thread 字段返回 RecordedThread（多态通道）。
### 实测负例异常表（类型错配矩阵，双侧消息一致）
| 操作 | 异常 | 精确消息 |
|---|---|---|
| `getInt("longField")`（收窄） | IllegalArgumentException | `Attempt to get field "longField" with illegal data type conversion long -> int` |
| `getShort("intField")` / `getByte("intField")` | IllegalArgumentException | `Attempt to get field "intField" with illegal data type conversion int -> short/byte` |
| `getBoolean/getChar/getDouble/getFloat/getString("String字段")` | IllegalArgumentException | `Attempt to get field "..." with illegal data type conversion java.lang.String -> ...` |
| `getLong/getString/getThread/getDuration/getInstant` 类型不符 | IllegalArgumentException | 同上格式 |
| `getClass("longField")` / `getClass("booleanField")` | IllegalArgumentException | 同上格式 |
| getter(不存在的字段 "nope") | IllegalArgumentException | `Could not find field with name nope` |
| `getString("nope")` | IllegalArgumentException | `Attempt to get unknown field "nope"` |
| `getInstant(无 @Timestamp 的 long 字段)` | IllegalArgumentException | `missing @Timestamp` |
| `getDuration("durField")`（Duration 不是合法 JFR 字段类型，8 侧字段被静默丢弃） | IllegalArgumentException | `Could not find field with name durField` |
### 8/27 差异
27 侧同样矩阵全部成立；差异仅在 Duration 字段是否被注册表接受（双侧均不合法）与隐式字段集合（27 有 isVirtual 相关线程语义）。

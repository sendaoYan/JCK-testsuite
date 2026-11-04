# java.nio.BufferOverflowException 设计文档

## 1. API 功能

向缓冲区写入超过其 capacity 的数据时抛出的运行时异常。

- 构造器：`BufferOverflowException()`（唯一构造器，无 message）。
- 继承链：`BufferOverflowException` → `RuntimeException` → `Exception` → `Throwable`。
- 继承的查询/链方法：`getMessage()`、`getCause()`、`getStackTrace()`。

## 2. Java 用户如何使用

- 捕获型用法：`ByteBuffer.allocate(n).put(...)` 越过容量时 `catch (BufferOverflowException e)`。
- 组合链路：`allocate` → `put` → 越界 `put` 抛异常；或 `CoderResult.OVERFLOW.throwException()` 抛出该异常。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/`）：各 Buffer 实现的写入路径在越界时抛出 `BufferOverflowException`。
- `CoderResult` 源码（`src/java.base/share/classes/java/nio/charset/CoderResult.java`）：`throwException()` 的 `CR_OVERFLOW` 分支抛出 `BufferOverflowException`。

## 4. 测试用例设计 + 自动化实现

文件：`BufferOverflowExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `RuntimeException` / 无参构造器。
- 黑盒：直接 `new` → message null → cause null → 堆栈非空；`ByteBuffer.allocate(1).put` 触发该异常。


### 4.A2 批次深化（追加；JDK-8 + JDK-27 双侧同构）

- `testClassContract`：serialVersionUID 固定值 -5484897634319144535（ObjectStreamClass.lookup 实测；JDK-8
  jdk8u372-b07 与 JDK-27 27.0.1.0.2+1 **相同**，跨版本序列化形式兼容）；唯一 public 构造器为无参
  （`getConstructors().length==1`），无 String message 构造器（getConstructor(String.class) 抛
  NoSuchMethodException——java.nio 异常风格，区别于多数 RuntimeException 子类；javap 双侧确认）。
- `testSerializationRoundTrip`：new 出的实例 + 库操作真实抛出的实例（captureTrigger）双 round-trip
  （ByteArray/Object 流）：精确类保持、父类链保持（RuntimeException）、getMessage()==null 与 getCause()==null
  保持（本类无 message 构造器，消息恒 null）。toString() == "java.nio.BufferOverflowException"（null message 的默认形态）。
- `testTypedBufferTriggers`：真实触发矩阵——相对 put 到满 ShortBuffer；IntBuffer bulk put 剩余不足；LongBuffer put(LongBuffer) 剩余不足；
  DoubleBuffer 满 put（断言失败后 position 不变）；FloatBuffer bulk put 带非法 offset（越界 IndexOOB
  优先于 overflow）。

实测事实（javap + 探针，双侧一致）：BufferOverflowException extends RuntimeException；仅无参构造器；Serializable，
serialVersionUID=-5484897634319144535（JDK-8 与 JDK-27 相同）。

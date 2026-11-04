# java.nio.BufferUnderflowException 设计文档

## 1. API 功能

从缓冲区读取超过其 limit 的数据时抛出的运行时异常。

- 构造器：`BufferUnderflowException()`（唯一构造器，无 message）。
- 继承链：`BufferUnderflowException` → `RuntimeException` → `Exception` → `Throwable`。
- 继承的查询/链方法：`getMessage()`、`getCause()`、`getStackTrace()`。

## 2. Java 用户如何使用

- 捕获型用法：`flip()` 后 `get()` 越过 limit 时 `catch (BufferUnderflowException e)`。
- 组合链路：`allocate` → `put` → `flip` → 正常 `get` → 越界 `get` 抛异常；
  或 `CoderResult.UNDERFLOW.throwException()` 抛出该异常。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/`）：各 Buffer 实现的读取路径在越过 limit 时抛出 `BufferUnderflowException`。
- `CoderResult` 源码（`src/java.base/share/classes/java/nio/charset/CoderResult.java`）：`throwException()` 的 `CR_UNDERFLOW` 分支抛出 `BufferUnderflowException`。

## 4. 测试用例设计 + 自动化实现

文件：`BufferUnderflowExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `RuntimeException` / 无参构造器。
- 黑盒：直接 `new` → message null → cause null → 堆栈非空；`allocate→put→flip→get→越界 get` 触发该异常。


### 4.A2 批次深化（追加；JDK-8 + JDK-27 双侧同构）

- `testClassContract`：serialVersionUID 固定值 -1713313658691622206（ObjectStreamClass.lookup 实测；JDK-8
  jdk8u372-b07 与 JDK-27 27.0.1.0.2+1 **相同**，跨版本序列化形式兼容）；唯一 public 构造器为无参
  （`getConstructors().length==1`），无 String message 构造器（getConstructor(String.class) 抛
  NoSuchMethodException——java.nio 异常风格，区别于多数 RuntimeException 子类；javap 双侧确认）。
- `testSerializationRoundTrip`：new 出的实例 + 库操作真实抛出的实例（captureTrigger）双 round-trip
  （ByteArray/Object 流）：精确类保持、父类链保持（RuntimeException）、getMessage()==null 与 getCause()==null
  保持（本类无 message 构造器，消息恒 null）。toString() == "java.nio.BufferUnderflowException"（null message 的默认形态）。
- `testTypedBufferTriggers`：真实触发矩阵——空 ShortBuffer 相对 get；IntBuffer bulk get 剩余不足；FloatBuffer compact 后读到 limit 之外
  （compact 留 limit-pos 可读，读尽再 get 抛）；DoubleBuffer bulk get（失败后 position 不变）；
  LongBuffer bulk get 带非法 offset（越界 IndexOOB 优先于 underflow）。

实测事实（javap + 探针，双侧一致）：BufferUnderflowException extends RuntimeException；仅无参构造器；Serializable，
serialVersionUID=-1713313658691622206（JDK-8 与 JDK-27 相同）。

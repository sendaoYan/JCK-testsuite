# java.nio.ReadOnlyBufferException 设计文档

## 1. API 功能

对只读缓冲区执行修改操作（如 `put`）时抛出的运行时异常。

- 构造器：`ReadOnlyBufferException()`（唯一构造器，无 message）。
- 继承链：`ReadOnlyBufferException` → `UnsupportedOperationException` → `RuntimeException` → `Exception` → `Throwable`。
- 继承的查询/链方法：`getMessage()`、`getCause()`、`getStackTrace()`。

## 2. Java 用户如何使用

- 捕获型用法：`buffer.asReadOnlyBuffer().put(...)` 时 `catch (ReadOnlyBufferException e)`。
- 组合链路：`allocate` → `asReadOnlyBuffer`（`isReadOnly()==true`）→ `put` 抛异常。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/`）：只读缓冲区的可变操作在 `isReadOnly()` 时抛出 `ReadOnlyBufferException`。

## 4. 测试用例设计 + 自动化实现

文件：`ReadOnlyBufferExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `UnsupportedOperationException` / 无参构造器。
- 黑盒：直接 `new` → 层级 → message null → cause null → 堆栈非空；`asReadOnlyBuffer().put` 触发该异常。


### 4.A2 批次深化（追加；JDK-8 + JDK-27 双侧同构）

- `testClassContract`：serialVersionUID 固定值 -1210063976496234090（ObjectStreamClass.lookup 实测；JDK-8
  jdk8u372-b07 与 JDK-27 27.0.1.0.2+1 **相同**，跨版本序列化形式兼容）；唯一 public 构造器为无参
  （`getConstructors().length==1`），无 String message 构造器（getConstructor(String.class) 抛
  NoSuchMethodException——java.nio 异常风格，区别于多数 RuntimeException 子类；javap 双侧确认）。
- `testSerializationRoundTrip`：new 出的实例 + 库操作真实抛出的实例（captureTrigger）双 round-trip
  （ByteArray/Object 流）：精确类保持、父类链保持（UnsupportedOperationException）、getMessage()==null 与 getCause()==null
  保持（本类无 message 构造器，消息恒 null）。toString() == "java.nio.ReadOnlyBufferException"（null message 的默认形态）。
- `testTypedBufferTriggers`：真实触发矩阵——ShortBuffer.asReadOnlyBuffer() 的相对 put/绝对 put/bulk put/put(XBuffer)/compact/array() 全矩阵；
  只读 ByteBuffer 的 asFloatBuffer() 视图 put；正例：只读缓冲游标仍可变、读仍可用。

实测事实（javap + 探针，双侧一致）：ReadOnlyBufferException extends UnsupportedOperationException；仅无参构造器；Serializable，
serialVersionUID=-1210063976496234090（JDK-8 与 JDK-27 相同）。

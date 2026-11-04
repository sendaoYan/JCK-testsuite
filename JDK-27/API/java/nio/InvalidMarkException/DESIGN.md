# java.nio.InvalidMarkException 设计文档

## 1. API 功能

缓冲区没有有效 mark 时调用 `reset()` 抛出的运行时异常。

- 构造器：`InvalidMarkException()`（唯一构造器，无 message）。
- 继承链：`InvalidMarkException` → `IllegalStateException` → `RuntimeException` → `Exception` → `Throwable`。
- 继承的查询/链方法：`getMessage()`、`getCause()`、`getStackTrace()`。

## 2. Java 用户如何使用

- 捕获型用法：`buffer.reset()` 但此前未 `mark()` 时 `catch (InvalidMarkException e)`。
- 组合链路：`mark()` → `position(n)` → `reset()` 回到标记位；`clear()`（丢弃 mark）→ `reset()` 抛异常。

## 3. 开源代码实际用法调研

- JDK 自身实现（`~/git/jdk25u-dev/src/java.base/share/classes/java/nio/Buffer.java`）：`reset()` 在 `mark < 0` 时抛出 `InvalidMarkException`；`clear/rewind/flip` 等操作会丢弃 mark。

## 4. 测试用例设计 + 自动化实现

文件：`InvalidMarkExceptionTest.java`（testWhiteBox + testBlackBox）。

- 白盒：public / 非 final / 父类 `IllegalStateException` / 无参构造器。
- 黑盒：直接 `new` → 层级 → message null → cause null → 堆栈非空；`mark→position→reset` 正常路径 +
  `reset`（无 mark）与 `clear→reset` 两条异常路径。


### 4.A2 批次深化（追加；JDK-8 + JDK-27 双侧同构）

- `testClassContract`：serialVersionUID 固定值 1698329710438510774（ObjectStreamClass.lookup 实测；JDK-8
  jdk8u372-b07 与 JDK-27 27.0.1.0.2+1 **相同**，跨版本序列化形式兼容）；唯一 public 构造器为无参
  （`getConstructors().length==1`），无 String message 构造器（getConstructor(String.class) 抛
  NoSuchMethodException——java.nio 异常风格，区别于多数 RuntimeException 子类；javap 双侧确认）。
- `testSerializationRoundTrip`：new 出的实例 + 库操作真实抛出的实例（captureTrigger）双 round-trip
  （ByteArray/Object 流）：精确类保持、父类链保持（IllegalStateException）、getMessage()==null 与 getCause()==null
  保持（本类无 message 构造器，消息恒 null）。toString() == "java.nio.InvalidMarkException"（null message 的默认形态）。
- `testTypedBufferTriggers`：真实触发矩阵——无 mark 的 ShortBuffer reset；IntBuffer mark 后 position 移到 mark 之下再 reset；
  LongBuffer mark 后 limit 移到 mark 之下再 reset；FloatBuffer 带 mark 的 buffer 其 slice 的 reset
  （slice 无 mark）；正例：DoubleBuffer mark 后 position 前移 reset 成功、可重复、duplicate 继承 mark。

实测事实（javap + 探针，双侧一致）：InvalidMarkException extends IllegalStateException；仅无参构造器；Serializable，
serialVersionUID=1698329710438510774（JDK-8 与 JDK-27 相同）。

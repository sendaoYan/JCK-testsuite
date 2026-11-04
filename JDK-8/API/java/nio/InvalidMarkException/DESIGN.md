# java.nio.InvalidMarkException 测试方案

## 1. API 功能
- extends IllegalStateException, no-arg 构造器, Serializable; 消息为 null

## 2. Java 用户如何使用
- 相对 get/put 越界 (Overflow/Underflow)、reset 无 mark (InvalidMark)、只读缓冲写 (ReadOnly) 时抛出

## 3. 开源代码实际用法调研
- jdk8u-dev: 这些异常继承自 IllegalStateException, 仅默认构造器

## 4. 测试用例设计 + 自动化实现
- 白盒: 父类/构造器/Serializable/消息 null; 黑盒: 真实触发场景 + 失败后缓冲状态可观察(position 不变/数据未写)。实现于 InvalidMarkExceptionTest.java

健壮性: 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖 (MappedByteBuffer 除外, 用临时文件
并在 finally 清理); 字节序断言显式 order(...) 固定布局不依赖平台 nativeOrder; 中文/emoji 用
Unicode 字面量固定; 固定输入跨平台一致.


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

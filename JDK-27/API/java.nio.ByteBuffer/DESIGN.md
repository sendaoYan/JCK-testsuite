# java.nio.ByteBuffer 测试方案

## 1. API 功能
- 工厂: `allocate(int)`, `allocateDirect(int)`, `wrap(byte[])`, `wrap(byte[],int,int)`
- 读写: 相对/绝对 `get()/get(int)/put(byte)/put(int,byte)`, 批量 `get(byte[],..)/put(byte[],..)`
- 字节序与类型化: `order()/order(ByteOrder)`, `getInt/putInt/getShort/putShort/getLong/putLong/getChar/putChar/getFloat/putFloat/getDouble/putDouble`
- 视图: `asIntBuffer()/asLongBuffer()/asCharBuffer()/asShortBuffer()/asFloatBuffer()/asDoubleBuffer()`
- 复制/切片/压缩: `duplicate()`, `slice()`, `slice(int,int)`, `compact()`, `asReadOnlyBuffer()`
- 比较: `equals`, `compareTo`, `mismatch`

## 2. Java 用户如何使用
- 构造: `ByteBuffer.allocate(8)` (堆) 或 `ByteBuffer.allocateDirect(8)` (直接).
- 字节序链路: `allocate(4).order(LITTLE_ENDIAN).putInt(x) -> flip() -> 逐字节读或 order(BIG_ENDIAN).getInt()`.
- 视图: `bb.asIntBuffer()` 以 int 元素读写同一块字节内存.
- 复制: `duplicate()`/`slice()` 共享字节但游标独立; `compact()` 把未读字节搬到头部以便继续写.

## 3. 开源代码实际用法调研
- `test/jdk/java/nio/channels/DatagramChannel/EmptyBuffer.java`: `bb.order(ByteOrder.BIG_ENDIAN); bb.putInt(1).putLong(1);` — 字节序 + 类型化写.
- `test/jdk/tools/jlink/plugins/CompressIndexesTest.java`: `ByteBuffer.allocate(totalLength)` — 容量分配.
- `test/jdk/java/foreign/TestByteBuffer.java`: `ByteBuffer::asCharBuffer` — 类型化视图.
- `test/jdk/java/foreign/TestSegmentAllocators.java`: `segment.asByteBuffer().order(layout.order()).asIntBuffer()` — 视图 + 字节序组合.

## 4. 测试用例设计 + 自动化实现
推导: 相对/绝对/批量读写 (position 推进)、字节序 (LITTLE/BIG 布局与切换)、视图共享、
duplicate/slice 游标独立、compact 语义、equals/compareTo/mismatch、只读/直接缓冲属性、
越界异常 (BufferUnderflow/OverflowException, IndexOutOfBoundsException, IllegalArgumentException)。
实现于 `ByteBufferTest.java` 的 `testBlackBox()`; 结构断言在 `testWhiteBox()`.

健壮性: 字节序断言全部用显式 `order(...)` 固定布局 (不依赖平台 nativeOrder); 浮点/整数
put-get 往返按精确值断言; 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖.

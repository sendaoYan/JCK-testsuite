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


### A1 批次深化（追加）
- runStateTable(): 19 行完整状态迁移表(put×k/position/mark/reset/limit/flip/get/rewind/compact/clear),
  每步断言 position/limit/remaining/hasRemaining + 终态内容; heap 与 direct 各执行一遍。
- wrap(array,offset,length) 矩阵: 越界参数全抛 IndexOutOfBoundsException(非 IAE)、几何
  (position=offset/limit=offset+length)、arrayOffset=0、null NPE。
- 相对 vs 绝对 get/put 全类型矩阵 + 三类异常精确区分: 相对 put 满→BufferOverflowException、
  相对 get 空→BufferUnderflowException、绝对索引越界→IndexOutOfBoundsException。
- order 语义: 默认 BIG_ENDIAN(heap/direct/wrap)、order 返回 this、BE/LE 精确字节布局
  (0x0102030405060708L)、切换只改解释不改字节、6 种类型视图继承显式 order、实现稳定行为:
  slice/duplicate/asReadOnlyBuffer 将 order 重置为 BIG_ENDIAN(双侧双存储实测, javadoc 未承诺)。
- 视图族: asXxxBuffer(cap=remaining/pos 0/写回/LE-BE)、slice/duplicate/asReadOnly 独立游标+
  共享内容+只读矩阵、compact 语义(搬移 remaining、pos=remaining、lim=capacity、丢 mark)。
- equals/compareTo/hashCode 仅看 [position,limit) 剩余区域; 批量传输矩阵(put(this) IAE、NPE、零长度)。
- JDK-27 独有(带 JDK-13+/JDK-11+/JDK-9+ 注释): slice(int,int)(边界按 limit 校验)、
  get/put(int,byte[][,int,int])、put(int,ByteBuffer,int,int)、mismatch 全矩阵(相等-1/首个差异下标/
  前缀/相对 position/heap-vs-direct)、alignedSlice/alignmentOffset(direct 8 对齐保容量、heap
  unitSize>1 UOE、非法参数 IAE); JDK-8 侧用反射断言这些 API 缺失并记录。
- 断言数 91 -> 248 (JDK-8) / 74 -> 271 (JDK-27)。

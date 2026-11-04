# java.nio.ByteBuffer 测试方案

## 1. API 功能
- 工厂: allocate(int)/allocateDirect(int)/wrap(byte[])/wrap(byte[],int,int)
- 读写: 相对/绝对 get()/get(int)/put(byte)/put(int,byte), 批量 get/put(byte[],..), put(ByteBuffer)
- 字节序与类型化: order()/order(ByteOrder), getInt/putInt/getShort/putShort/getLong/putLong/getChar/putChar/getFloat/putFloat/getDouble/putDouble
- 视图: asIntBuffer()/asLongBuffer()/asCharBuffer()/asShortBuffer()/asFloatBuffer()/asDoubleBuffer()
- 复制/切片/压缩: duplicate()/slice()/compact()/asReadOnlyBuffer(); 比较 equals/compareTo/hashCode

## 2. Java 用户如何使用
- allocate(8) 堆 / allocateDirect(8) 直接
- 字节序链路: allocate(4).order(LITTLE_ENDIAN).putInt(x)->flip()->逐字节读或 order(BIG_ENDIAN).getInt()
- 视图 asIntBuffer() 以 int 元素读写同一块内存; duplicate/slice 共享底层独立游标; compact 把未读搬头部

## 3. 开源代码实际用法调研
- jdk8u-dev ByteBuffer: 直接缓冲 hasArray()==false, array() 抛 UOE; 只读堆缓冲 array() 抛 ReadOnlyBufferException; HeapByteBuffer 默认 BIG_ENDIAN

## 4. 测试用例设计 + 自动化实现
- 白盒: 抽象/extends Buffer/Comparable/工厂与类型化方法存在; 黑盒: 相对/绝对/批量读写、字节序布局与切换、视图共享、duplicate/slice/compact、equals/compareTo、只读/直接属性、越界异常(Underflow/Overflow/IOOBE/IAE)、allocate(0)/(-1)、mark/reset。实现于 ByteBufferTest.java

健壮性: 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖 (MappedByteBuffer 除外, 用临时文件
并在 finally 清理); 字节序断言显式 order(...) 固定布局不依赖平台 nativeOrder; 中文/emoji 用
Unicode 字面量固定; 固定输入跨平台一致.


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

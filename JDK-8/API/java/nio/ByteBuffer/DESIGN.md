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

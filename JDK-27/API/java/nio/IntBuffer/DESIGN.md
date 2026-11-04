# java.nio.IntBuffer 测试方案

## 1. API 功能
- 工厂: `allocate(int)`, `wrap(int[])`, `wrap(int[],int,int)`
- 读写: 相对/绝对 `get()/get(int)/put(int)/put(int,int)`, 批量 `get(int[],..)/put(int[],..)`
- 视图/复制: `duplicate()`, `slice()`, `slice(int,int)`, `compact()`, `asReadOnlyBuffer()`
- 属性/比较: `hasArray()`, `array()`, `arrayOffset()`, `order()`, `equals`, `compareTo`, `mismatch`

## 2. Java 用户如何使用
- 构造: `IntBuffer.wrap(int[])` 或 `IntBuffer.allocate(n)`.
- 组合链路: `wrap -> put(绝对) -> get(相对) -> flip() -> 逐元素读取; duplicate() 写回影响源`.
- `compact()` 把未读元素搬到头部.

## 3. 开源代码实际用法调研
- `test/jdk/java/foreign/TestMemoryAlignment.java`: `IntBuffer.allocate(8)` — 容量分配.
- `test/jdk/java/foreign/TestSegmentAllocators.java`: `segment.asByteBuffer().order(layout.order()).asIntBuffer()` — 由 ByteBuffer 得到 int 视图.
- JDK 源码 `java.nio.IntBuffer#wrap(int[],int,int)`: 共享底层 int[] 且设置 position/limit.

## 4. 测试用例设计 + 自动化实现
推导: 相对/绝对/批量读写 (position 推进)、wrap 语义 (capacity/position/limit)、duplicate/slice
共享存储且游标独立、compact、equals/compareTo/mismatch、只读、越界异常
(BufferUnderflow/OverflowException, IndexOutOfBoundsException, IllegalArgumentException)。
实现于 `IntBufferTest.java` 的 `testBlackBox()`; 结构断言在 `testWhiteBox()`.

健壮性: 固定 int 输入; 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖; 不依赖平台字节序.

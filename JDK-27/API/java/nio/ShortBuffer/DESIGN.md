# java.nio.ShortBuffer 测试方案

## 1. API 功能
- 工厂: `allocate(int)`, `wrap(short[])`, `wrap(short[],int,int)`
- 读写: 相对/绝对 `get()/get(int)/put(short)/put(int,short)`, 批量 `get(short[],..)/put(short[],..)`
- 视图/复制: `duplicate()`, `slice()`, `slice(int,int)`, `compact()`, `asReadOnlyBuffer()`
- 属性/比较: `hasArray()`, `array()`, `arrayOffset()`, `order()`, `equals`, `compareTo`, `mismatch`

## 2. Java 用户如何使用
- 构造: `ShortBuffer.wrap(short[])` 或 `ShortBuffer.allocate(n)`.
- 组合链路: `wrap -> put(绝对) -> get(相对) -> flip() -> 读取; duplicate() 共享存储`.
- `compact()` 压缩未读元素.

## 3. 开源代码实际用法调研
- `test/jdk/java/foreign/TestSegmentAllocators.java`: `asShortBuffer()` 视图模式 (同类).
- JDK 源码 `java.nio.ShortBuffer#wrap(short[],int,int)`: 共享底层 short[].
- `test/jdk/java/nio/channels/DatagramChannel/Sender.java`: ByteBuffer 上 `putInt(1).putLong(1)` 类型化写.

## 4. 测试用例设计 + 自动化实现
推导: 相对/绝对/批量读写、wrap 语义、duplicate/slice 共享存储与独立游标、compact、
equals/compareTo/mismatch、只读、越界异常。实现于 `ShortBufferTest.java` 的 `testBlackBox()`;
结构断言在 `testWhiteBox()`.

健壮性: 固定 short 输入; 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖; 不依赖平台字节序.

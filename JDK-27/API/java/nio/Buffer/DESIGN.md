# java.nio.Buffer 测试方案

## 1. API 功能
- 游标查询: `capacity()`, `position()`, `limit()`, `remaining()`, `hasRemaining()`
- 游标设置: `position(int)`, `limit(int)`
- 状态机复位: `mark()`, `reset()`, `clear()`, `flip()`, `rewind()`
- 存储/只读属性: `isReadOnly()`, `hasArray()`, `array()`, `arrayOffset()`, `isDirect()`
- 视图: `slice()`, `slice(int,int)`, `duplicate()`

## 2. Java 用户如何使用
- 构造: Buffer 抽象, 用户通过 `ByteBuffer.allocate(n)` 等子类工厂取得实例.
- 组合链路 (写->读): `allocate -> put* (position 前进) -> flip() (limit=position, position=0) -> get* 读取 -> clear()/rewind()`.
- 回退标记: `mark()` 记下当前 position, 之后 `reset()` 回到该点; position/limit 调到小于 mark 时 mark 被丢弃.
- 视图: `duplicate()`/`slice()` 返回共享底层存储但游标独立的新缓冲.

## 3. 开源代码实际用法调研
- `test/jdk/java/nio/Buffer/OrderShort.java`: `buf.duplicate().order()`, `buf.slice().order()`, `buf.asReadOnlyBuffer().order()` — 视图共享源字节序.
- `test/jdk/java/io/Writer/Bug6856817.java`: `charBuffer.flip()` — 写后翻转为读模式的典型用法.
- JDK 源码 `java.nio.Buffer#flip`: `limit = position; position = 0` — 与测试断言一致.

## 4. 测试用例设计 + 自动化实现
由 1-3 推导: 正常路径 (allocate -> position/limit -> remaining)、组合 (mark/reset/flip/rewind/clear)、
边界 (position/limit 负值或 >capacity 抛 IllegalArgumentException)、异常 (reset 无 mark 抛
InvalidMarkException)、视图 (slice/duplicate 共享存储、独立游标)。均实现于
`BufferTest.java` 的 `testBlackBox()`, 结构断言在 `testWhiteBox()`.

健壮性: 无墙钟/随机/哈希顺序/System.gc/全局状态/文件/网络/线程依赖; 不依赖平台字节序
(只用 nativeOrder() 断言是两常量之一); 固定输入, 跨平台一致.

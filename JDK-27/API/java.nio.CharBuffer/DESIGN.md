# java.nio.CharBuffer 测试方案

## 1. API 功能
- 工厂: `allocate(int)`, `wrap(char[])`, `wrap(char[],int,int)`, `wrap(CharSequence)`, `wrap(CharSequence,int,int)`
- 读写: 相对/绝对 `get()/get(int)/put(char)/put(int,char)`, 批量 `get(char[],..)/put(char[],..)`, `put(String)`, `put(String,int,int)`
- CharSequence 表面: `length()`, `isEmpty()`, `charAt(int)`, `subSequence(int,int)`, `toString()`
- Appendable/Readable: `append(char)`, `append(CharSequence)`, `append(CharSequence,int,int)`, `read(CharBuffer)`
- 视图/比较: `duplicate()`, `slice()`, `slice(int,int)`, `compact()`, `asReadOnlyBuffer()`, `equals`, `compareTo`, `mismatch`

## 2. Java 用户如何使用
- 构造: `CharBuffer.wrap("hello")` (CharSequence) 或 `CharBuffer.wrap(char[])` / `allocate`.
- 组合链路: `wrap -> append/put(String) -> flip() -> charAt/length/subSequence/toString`.
- `read(CharBuffer)` 从另一 CharBuffer 抽取字符; `toString()` 只返回 remaining 区间.

## 3. 开源代码实际用法调研
- `test/jdk/java/io/Writer/Bug6856817.java`: `charBuffer.flip()` — 写后翻转读取.
- `test/jdk/java/foreign/TestByteBuffer.java`: `ByteBuffer::asCharBuffer` — 由字节缓冲得到字符视图.
- JDK 源码 `java.nio.CharBuffer#wrap(CharSequence,int,int)`: 直接构造共享 CharSequence 内容.

## 4. 测试用例设计 + 自动化实现
推导: 相对/绝对/批量/String 读写、CharSequence 表面、append/read、wrap 语义 (capacity/position/
limit 正确)、duplicate/slice/compact、只读、equals/compareTo/mismatch、越界异常。
实现于 `CharBufferTest.java` 的 `testBlackBox()`; 结构断言在 `testWhiteBox()`.

健壮性: 固定字符输入; 无墙钟/随机/哈希顺序/全局状态/文件/网络/线程依赖; 不依赖平台字节序;
越界断言用精确异常类型.

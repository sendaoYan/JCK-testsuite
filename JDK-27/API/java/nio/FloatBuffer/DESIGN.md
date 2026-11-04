# java.nio.FloatBuffer 测试方案

## 1. API 功能
- 工厂: `allocate(int)`, `wrap(float[])`, `wrap(float[],int,int)`
- 读写: 相对/绝对 `get()/get(int)/put(float)/put(int,float)`, 批量 `get(float[],..)/put(float[],..)`
- 视图/复制: `duplicate()`, `slice()`, `slice(int,int)`, `compact()`, `asReadOnlyBuffer()`
- 属性/比较: `hasArray()`, `array()`, `arrayOffset()`, `order()`, `equals`, `compareTo`, `mismatch`

## 2. Java 用户如何使用
- 构造: `FloatBuffer.wrap(float[])` 或 `FloatBuffer.allocate(n)`.
- 组合链路: `wrap -> put(绝对) -> get(相对) -> flip() -> 读取; duplicate() 共享存储`.
- `compact()` 压缩未读元素; compareTo 用 Float.compare 语义.

## 3. 开源代码实际用法调研
- `test/jdk/java/foreign/TestSegmentAllocators.java`: `asFloatBuffer()` 视图模式 (同类).
- JDK 源码 `java.nio.FloatBuffer#wrap(float[],int,int)`: 共享底层 float[].
- JDK 源码 `java.nio.FloatBuffer#compareTo`: 用 Float.compare 逐元素比较.

## 4. 测试用例设计 + 自动化实现
推导: 相对/绝对/批量读写、wrap 语义、duplicate/slice 共享存储与独立游标、compact、
equals/compareTo/mismatch、只读、越界异常。实现于 `FloatBufferTest.java` 的 `testBlackBox()`;
结构断言在 `testWhiteBox()`.

健壮性: 固定可精确表示的 float 值 (1.5f 等) 往返按精确值断言, 不涉及超越函数; 无墙钟/随机/
哈希顺序/全局状态/文件/网络/线程依赖.

# java.io.Flushable 测试方案 (DESIGN)

## 1. API 功能
- `flush() throws IOException`：把缓冲数据强制写到下层。

## 2. Java 用户如何使用
- 缓冲流（BufferedOutputStream/Writer）在关键时刻 flush 落盘/落网；接口化统一调用。

## 3. 开源代码实际用法调研
- 定义 — `jdk25u-dev/src/java.base/share/classes/java/io/Flushable.java`。
- 语义 — `jdk25u-dev/src/java.base/share/classes/java/io/BufferedOutputStream.java`（flush 把 buf 写入下层）。

## 4. 测试用例设计 + 自动化实现（FlushableTest.java）
- 白盒: 接口、根接口（无父）、flush 声明 IOException、OutputStream/Writer 实现它。
- 黑盒场景:
  1. lambda 计数。
  2. 缓冲流写入未到下层、flush 后到达（可观测下沉）。
  3. flush 抛 IOException 传播。
  4. 无缓冲流 flush no-op。
  5. close 也会 flush 缓冲。
  6. flush 可重复。
- 健壮性: 全内存。

产出：`FlushableTest.java`（白盒 7 + 黑盒 7 断言）。

# java.io.Closeable 测试方案 (DESIGN)

## 1. API 功能
- `close() throws IOException`，extends AutoCloseable；流/读/写资源释放的标准接口。

## 2. Java 用户如何使用
- try-with-resources 自动调用；手工资源管理；lambda 适配第三方资源。

## 3. 开源代码实际用法调研
- 定义 — `jdk25u-dev/src/java.base/share/classes/java/io/Closeable.java`（close 幂等要求）。
- 官方测试 — `jdk25u-dev/test/jdk/lang/exceptions/`（TWR 行为）。

## 4. 测试用例设计 + 自动化实现（CloseableTest.java）
- 白盒: 接口、extends AutoCloseable、close 声明 IOException、InputStream/Reader 实现它。
- 黑盒场景:
  1. lambda 实现 + instanceof AutoCloseable。
  2. TWR 恰好 close 一次、块内未 close。
  3. 异常路径先 close 再抛出。
  4. close 自身抛 IOException 传播。
  5. 多资源逆序关闭。
  6. 幂等 close（实现方自证）。
  7. BAIS 经接口使用。
- 健壮性: 全内存。

产出：`CloseableTest.java`（白盒 7 + 黑盒 10 断言）。

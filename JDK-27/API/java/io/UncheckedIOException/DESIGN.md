# java.io.UncheckedIOException 测试方案 (DESIGN)

## 1. API 功能
- `(IOException cause)` / `(String message, IOException cause)`：把 IOException 包装为不受检异常；`getCause()` 强类型返回 IOException；cause 不允许 null（requireNonNull）。

## 2. Java 用户如何使用
- 在不允许 throws IOException 的 API（Stream、Runnable、方法引用）里传播 I/O 失败；接收方 catch 后 `getCause()` 解包处理。

## 3. 开源代码实际用法调研
- 类型定义（requireNonNull、getCause 类型化）— `jdk25u-dev/src/java.base/share/classes/java/io/UncheckedIOException.java:49-73`。
- 典型抛出 — `jdk25u-dev/src/java.base/share/classes/java/io/BufferedReader.java`（lines() 内部包装 IOError/UncheckedIOException 路径）与 nio Files 遍历流。

## 4. 测试用例设计 + 自动化实现（UncheckedIOExceptionTest.java）
- 白盒: extends RuntimeException、两构造、getCause 声明返回 IOException。
- 黑盒场景:
  1. (IOException) 构造：message=cause.toString()。
  2. (String, IOException) 构造。
  3. null cause → NPE。
  4. 不受检传播（Runnable 无 throws）。
  5. 真实触发：BufferedReader.lines() 读取中底层 Reader 抛 IOException → 包装为 UIO 且 cause 可解包（两层验证）。
  6. 恢复模式：解包并重抛受检 IOException。
- 健壮性: 自定义 Reader 固定第 2 次调用失败，确定性强；无真实 I/O。

产出：`UncheckedIOExceptionTest.java`（白盒 5 + 黑盒 6 断言）。

# java.io.UncheckedIOException 测试方案 (DESIGN)

## 1. API 功能
- `(IOException)` 与 `(String, IOException)` 构造；继承 RuntimeException；getCause 协变返回 IOException。

## 2. Java 用户如何使用
- 在不能抛受检异常的流式 API（如 BufferedReader.lines()）中包装 IOException。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/UncheckedIOException.java`。

## 4. 测试用例设计 + 自动化实现（UncheckedIOExceptionTest.java，纯黑盒）
黑盒场景:
1. `(IOException)`：cause 保留、消息=cause.toString()。
2. `(String, IOException)`：消息 + cause。
3. null cause 拒收 NPE。
4. 未检查传播。
5. BufferedReader.lines() 中途 IOException 包装为 UncheckedIOException。
6. 捕获后解包恢复受检 IOException。
- 健壮性: 无反射白盒。

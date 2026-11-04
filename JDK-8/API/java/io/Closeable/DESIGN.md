# java.io.Closeable 测试方案 (DESIGN)

## 1. API 功能
- 扩展 `AutoCloseable`，声明 `close() throws IOException`；try-with-resources 自动调用。
- 多资源按声明逆序关闭；close 抛出的 IOException 传播；实现可选择幂等。

## 2. Java 用户如何使用
- `try (Closeable c = open()) { ... }` 保证退出时关闭；自定义类实现 close 释放资源。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/Closeable.java`（InputStream/Reader 皆实现）。

## 4. 测试用例设计 + 自动化实现（CloseableTest.java，纯黑盒）
黑盒场景:
1. lambda 实现 close 执行；运行时为 AutoCloseable。
2. try-with-resources 块内未关闭、退出恰好关闭一次。
3. 块内异常时 close 先于异常传播执行。
4. close 失败 IOException 传播。
5. 多资源逆序关闭。
6. 幂等 close 连续调用安全。
7. JDK 类（ByteArrayInputStream）经接口使用。
- 健壮性: 无反射白盒。

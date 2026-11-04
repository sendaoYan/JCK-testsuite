# AutoCloseable — 设计方案

## 1. API 功能
`java.lang.AutoCloseable`（接口）是 try-with-resources 的资源抽象，声明唯一抽象方法 `void close() throws Exception`，语义：资源在离开 try 块时按声明逆序自动关闭，且每个资源恰好关闭一次。

## 2. Java 用户如何使用
- 自定义资源实现 `AutoCloseable` 后放入 try-with-resources，JDK 自动调用 `close()`。
```java
try (Resource a = new Resource("a"); Resource b = new Resource("b")) { ... }
```
- `close()` 抛出的异常会从 try 块传播。

## 3. 开源代码实际用法调研
- JDK 内 `java.io`/`java.sql` 大量类型实现 `AutoCloseable`；`test/jdk/java/lang/AutoCloseable` 验证逆序关闭、单次关闭与异常传播。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.AutoCloseable")` 验证为接口、`close()` 返回 void 且声明 `throws Exception`。
黑盒：两个内嵌资源类记录关闭顺序，验证逆序 + 恰好一次；抛异常资源验证异常传播。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。

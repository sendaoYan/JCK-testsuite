# Readable — 设计方案

## 1. API 功能
`java.lang.Readable`（接口）声明唯一抽象方法 `int read(CharBuffer cb) throws IOException`：把字符读入缓冲区并返回读取数量，读到末尾返回 -1。

## 2. Java 用户如何使用
- 文本源（如 `StringReader`、`Reader` 的包装）实现 `Readable`，配合 `Scanner` 消费。
```java
Readable src = new StringReader("hello");
Scanner s = new Scanner(src);
```

## 3. 开源代码实际用法调研
- `java.util.Scanner(Readable)`、`java.io.Reader`（实现 `Readable`）在 JDK 中广泛使用；`test/jdk/java/lang/Readable` 验证读取计数与 EOF。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Readable")` 验证为接口，`read(CharBuffer)` 返回 int 且声明 `IOException`。
黑盒：`StringReader` 分两次读取 "hello" 验证计数与内容、EOF 返回 -1、`read(null)` 抛 NPE。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。

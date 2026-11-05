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

## 契约深化（2026-11）

本轮追加 11 个 test 方法（断言 9 → 44）：CharArrayReader 路径 + 零空间语义 + 只读缓冲 +
缓冲状态推进 + 自定义实现调用记录。

### 新增维度

- **CharArrayReader 路径**：容量 2 → 分块 2/1/-1 三段精确。
- **零剩余空间语义**：`read(CharBuffer.allocate(0))` 返回 **0**（有数据也不 -1 不抛），
  随后数据仍完整可读——0 与 -1 的区分是接口最易错点。
- **只读缓冲**：`asReadOnlyBuffer()` → `java.nio.ReadOnlyBufferException` 精确类型。
- **缓冲状态推进**：`position()` 增加量 == 返回值；flip 后须 clear 才能续读
  （flip 后 remaining 仍为已读数，耗尽的 reader 返回 -1 而非 0——已固化）。
- **包装数组可见性**：`CharBuffer.wrap(char[])` 写入直达底层数组、未写尾部不动。
- **自定义 Readable**：返回值 7/-1/0 原样穿透；收到的 CharBuffer 实例与传入同一（==）；
  抛出的 IOException **同一实例**穿透。
- **实现归属**：StringReader/CharArrayReader/BufferedReader instanceof Readable；
  CharArrayReader/BufferedReader 的 `read((CharBuffer) null)` NPE。
- **序列重组**：3/3/2 分块重组 "abcdefgh"、EOF 反复 -1、空输入立即 -1、短输入部分填充
  （容量 10 只读 2）。

### 易错规范细节（新增锁定）

- `read(null)` 与 `read(char[])` 重载二义，必须显式 `(CharBuffer) null`。
- 三值语义：n>0 / 0（无剩余）/ -1（流尾）——JDK-8 与 JDK-27 行为一致（双侧实测）。

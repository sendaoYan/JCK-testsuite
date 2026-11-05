# java.lang.Readable 测试设计案（JDK-8）

> 覆盖目标：read(CharBuffer) 的计数/EOF/零空间语义 + 缓冲状态推进 + 包装数组可见性 +
> 自定义实现调用记录 + 多 Reader 实现一致性。

## 1. API 提供了哪些功能

- 唯一抽象方法：`int read(CharBuffer cb) throws IOException`——尝试把字符读入缓冲区，
  返回读入数量；流尾返回 -1；**缓冲区无剩余空间返回 0**。
- JDK-8 实现类：`java.io.Reader` 全家族（StringReader/CharArrayReader/BufferedReader…）、
  `java.util.Scanner` 的输入端。

## 2. Java 用户如何使用

- 文本源实现 Readable 后交给 `java.util.Scanner(Readable)`；
- 或接口多态消费任意字符源。

## 3. 参考来源

- Oracle Java SE 8 javadoc（Readable、Reader.read(CharBuffer)）：`len == 0 时返回 0`、
  流尾 -1、只读缓冲抛 `java.nio.ReadOnlyBufferException`。
- 双侧实测（jdk8u372-b07 vs jdk-27.0.1.0.2+1）：0 容量缓冲在有数据时返回 0（两侧一致）、
  只读缓冲抛 ReadOnlyBufferException（两侧一致）、CharArrayReader 分块 2/1/-1。

## 4. 测试用例设计 + 易错规范细节

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | StringReader 分块读取计数/内容/EOF、read(null) NPE | testRead/testNull（既有） |
| P0 | CharArrayReader 路径：容量 2 → 2/1/-1 三段 | testCharArrayReaderPath |
| P0 | 0 剩余空间缓冲返回 0（有数据也不抛不返回 -1） | testZeroRemainingBuffer |
| P0 | 只读缓冲 → java.nio.ReadOnlyBufferException 精确类型 | testReadOnlyBuffer |
| P1 | position 随读取数推进；flip 后再读需 clear | testBufferPositionAdvances |
| P1 | CharBuffer.wrap(char[]) 写入对底层数组可见、未写尾部不动 | testWrappedArrayVisibility |
| P1 | 自定义 Readable：返回值 7/-1/0 原样穿透 + 收到的缓冲实例同一（==） | testCustomReadablePassthrough |
| P1 | 自定义 Readable 抛出的 IOException **同一实例**穿透 | testCustomReadableException |
| P1 | StringReader/CharArrayReader/BufferedReader instanceof Readable | testImplAssignment |
| P1 | 3/3/2 分块重组 "abcdefgh" + EOF 稳定 -1 | testSequentialAccumulation |
| P1 | 空输入立即 -1；短输入部分填充（容量 10 只读 2） | testEmptyInput |
| P2 | CharArrayReader/BufferedReader 的 read((CharBuffer)null) NPE | testNullBufferOtherImpl |

### 易错规范细节（已锁入断言）

- `read(CharBuffer)` 的三值语义：**n>0 读入数 / 0 缓冲无剩余（有数据也返回 0，不是 -1）/
  -1 流尾**——0 与 -1 的区分是本接口最易错点。
- `flip()` 后 position=0、limit=旧 position，remaining 不为 0；耗尽的 reader 此时返回 -1
  （不是 0）——flip/clear 语义与 read 交互易混，已用断言固化。
- `read(null)` 在 Reader 家族一致 NPE（注意与 `read(char[])` 重载二义，需显式 `(CharBuffer) null`）。
- 只读缓冲在 Reader.read 中抛 `java.nio.ReadOnlyBufferException`（RuntimeException，两侧一致）。

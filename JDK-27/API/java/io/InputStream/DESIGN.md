# java.io.InputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 抽象 `read()`；`read(byte[])`/`read(byte[],int,int)` 默认实现循环调用 read()；`readAllBytes`/`readNBytes(int)`/`readNBytes(byte[],int,int)`；`skip`/`skipNBytes`；`available`/`close` 默认 0/no-op；`mark`/`reset`/`markSupported` 默认不支持；`transferTo(OutputStream)`；静态 `nullInputStream()`。

## 2. Java 用户如何使用
- 自定义字节源只需实现 read()，其余继承默认行为；消费方按接口编程（read/skip/transferTo）。
- `nullInputStream()` 用于测试或占位。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- 默认实现 — `jdk25u-dev/src/java.base/share/classes/java/io/InputStream.java`（read(byte[],int,int) 循环、skip 默认、readNBytes/skipNBytes 语义、transferTo）。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/InputStream/`（readAllBytes/readNBytes/transferTo/skipNBytes 矩阵）。

## 4. 测试用例设计 + 自动化实现（InputStreamTest.java）
- 白盒: abstract、Closeable、read() 抽象、read(byte[],int,int) 具体、nullInputStream 静态、无 lock 字段。
- 黑盒场景（最小子类只实现 read()）:
  1. 派生 bulk read/offset read/EOF/-1。
  2. 零长读取不消耗字节。
  3. skip 正向/负向(negative→0)。
  4. skipNBytes 精确、负数 no-op、越界 EOFException。
  5. readAllBytes 排干流。
  6. readNBytes 两种形态 + 负数 IAE。
  7. transferTo 计数与内容、null NPE。
  8. available=0、close no-op、markSupported false、mark 不消耗、reset → "mark/reset not supported"。
  9. read(null) NPE、越界 IOOBE。
  10. nullInputStream 全 EOF。
  11. ByteArrayInputStream 经基类引用使用。
- 健壮性: 全内存，无时序。

产出：`InputStreamTest.java`（白盒 15 + 黑盒 36 断言）。

## 深化追加（契约级）

新增维度（全部经 JDK-27 实测锁定）：
- read(byte[]) 委托 read(byte[],int,int)（Spy 探针）；JDK-27 精确消息断言（Range/NPE 指名 b）
- readNBytes(int)：-1 → IAE("len < 0")、0 → 空数组、部分可用、MAX_VALUE 惰性不抛、EOF → 空数组
- readNBytes(b,off,len)：越界 Range 消息、null NPE、(b,0,0)=0、部分填充、EOF 返回 0（非 -1）
- skipNBytes：负数/0/Long.MIN_VALUE no-op、空源与 EOF 处正数 → EOFException、精确剩余后 EOF 抛
- transferTo：null → NPE("out")、read/write 侧 IOException 消息逐字透传、空流 0、优先 write(b,off,len) 通道
- nullInputStream 全矩阵：全 EOF、readNBytes(b)=0、readNBytes(int)=空数组、readAllBytes=空、skipNBytes(正) EOFException、mark ok / reset "mark/reset not supported"、close 幂等、关闭后 read/readNBytes(int)/readAllBytes/skip/available/transferTo → IOException("Stream closed")（小写 c）、关闭后 mark 仍允许
- skip 默认矩阵（0/负/Long.MIN/5000/剩余）、reset 精确消息、EOF×零长度交叉

负例矩阵：read(null) NPE / read(b,3,0) IOOBE(Range 消息) / readNBytes(-1) IAE("len < 0") / readNBytes(b,0,-1) IOOBE / transferTo(null) NPE("out") / nullInputStream.skipNBytes(2) EOFException / 关闭后六种操作 IOException("Stream closed")。

版本差异记录：基类 read 越界消息 JDK-8 null vs 27 "Range [...]"；NPE 消息 8 null vs 27 指名参数；nullInputStream.transferTo(null) 的 NPE 消息为 null（覆写版用单参 requireNonNull），区别于基类的 "out"。

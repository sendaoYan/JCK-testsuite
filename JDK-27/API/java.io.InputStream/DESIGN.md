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

# java.io.ByteArrayInputStream 测试方案 (DESIGN)

## 1. API 功能
- 构造: `ByteArrayInputStream(byte[])`、`ByteArrayInputStream(byte[],int,int)`
- 读: `read()`、`read(byte[],int,int)`、`readNBytes(byte[],int,int)`、`readAllBytes()`
- 定位/状态: `skip(long)`、`available()`、`markSupported()`、`mark(int)`、`reset()`
- 传输: `transferTo(OutputStream)`、`close()`（no-op）

## 2. Java 用户如何使用
- `new ByteArrayInputStream(bytes)` 把内存字节包装为 InputStream；`readAllBytes()` 一次性取回。
- 组合链路：`ByteArrayOutputStream.toByteArray()` → `ByteArrayInputStream` → `readAllBytes`/`transferTo` 往返；`read(buf,off,len)` 精确读入指定偏移。

## 3. 开源代码实际用法调研（真实片段）
- `DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));` — `jdk25u-dev/src/java.base/share/classes/java/time/zone/TzdbZoneRulesProvider.java:134`
- `new ByteArrayInputStream(buf, len - n, n)` — `jdk25u-dev/src/java.base/share/classes/java/util/zip/GZIPInputStream.java:282`
- `man.read(new ByteArrayInputStream(bytes));` — `jdk25u-dev/src/java.base/share/classes/java/util/jar/JarInputStream.java:148`

## 4. 测试用例设计 + 自动化实现（指向 ByteArrayInputStreamTest.java 黑盒 `testBlackBox`）
- 正常/组合: read()/read(byte[],off,len)/skip/available → EOF=-1；mark/reset 重放；未 mark reset 回起点；offset/length 构造；readAllBytes/transferTo 与 ByteArrayOutputStream 往返。
- 边界: 末尾 read()==-1、read(byte[])==-1；readNBytes 在末尾返回实际剩余数；close 后读仍返回数据（no-op）。
- 健壮性: 固定字节数组输入，无全局副作用，无墙钟/随机/集合顺序依赖。
- 白盒 `testWhiteBox`: 继承 InputStream、关键方法(readAllBytes/readNBytes/transferTo/markSupported)存在。

# java.io.FileInputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(File)`、`(String path)`、`(FileDescriptor)`。
- 读：`read()`、`read(byte[])`、`read(byte[],int,int)`、`skip(long)`、`available()`。
- 资源：`getFD()`、`getChannel()`、`close()`。
- 注意：`readAllBytes/readNBytes/transferTo` 为 JDK 9+，本 JDK-8 用例不含（改用 read 循环 drain）。

## 2. Java 用户如何使用
- `try (FileInputStream fis = new FileInputStream(new File(p))) { byte[] b = new byte[fis.available()]; fis.read(b); }` 读文件字节。
- 与 FileOutputStream 配对做文件复制/校验；`getChannel()` 提供 NIO FileChannel 视图。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `new FileInputStream(f)` + `available()`/`read(byte[])` 组合 — `jdk8u-dev/jdk/src/share/classes/java/io/FileInputStream.java`（readBytes 实现）。
- 文件复制惯用法（read→write 循环）— `jdk8u-dev/jdk/src/share/classes/sun/` 内多处工具流。

## 4. 测试用例设计 + 自动化实现（FileInputStreamTest.java，纯黑盒）
黑盒场景:
1. FileOutputStream 写已知字节 → FileInputStream(File) 读回：getFD/getChannel 非空、available 初值==剩余、read() 单字节、read(byte[],off,len) 区域、skip、EOF 后 available==0/read()==-1。
2. String 构造 + read 循环 drain 全量比对。
3. read(byte[]) 满量读 + EOF。
4. 打开不存在文件 → FileNotFoundException。
- 健壮性: 临时目录 createTempFile+mkdir，finally 递归清理；无 java.nio 依赖。

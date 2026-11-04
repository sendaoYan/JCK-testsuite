# java.io.FileOutputStream 测试方案 (DESIGN)

## 1. API 功能
- 构造: `FileOutputStream(String)`、`FileOutputStream(String,boolean)`、`FileOutputStream(File)`、`FileOutputStream(File,boolean)`、`FileOutputStream(FileDescriptor)`
- 写: `write(int)`、`write(byte[])`、`write(byte[],int,int)`
- 底层访问: `getFD()`、`getChannel()`、`close()`

## 2. Java 用户如何使用
- `try (FileOutputStream fos = new FileOutputStream(file))` 覆盖写；`new FileOutputStream(file, true)` 追加写。
- 组合链路：写 "ab" → 追加 "cd" → `FileInputStream` 读回 "abcd"；`write(byte[],off,len)` 只写子区间。

## 3. 开源代码实际用法调研（真实片段）
- `new JarOutputStream(new FileOutputStream(tempFile.toFile()))` — `jdk25u-dev/test/jdk/tools/lib/tests/JImageGenerator.java:137`
- `try (FileOutputStream fout = new FileOutputStream(cacerts.toFile())) {` — `jdk25u-dev/test/jdk/tools/jlink/runtimeImage/UpgradeableFileCacertsTest.java:138`
- `try (FileOutputStream fos = new FileOutputStream(jar.toFile());` — `jdk25u-dev/test/jdk/tools/jmod/JmodNegativeTest.java:264`

## 4. 测试用例设计 + 自动化实现（指向 FileOutputStreamTest.java 黑盒 `testBlackBox`）
- 正常/组合: 覆盖写 3 字节 → 追加 2 字节 → 读回 [1,2,3,4,5]；默认模式截断；`write(byte[],off,len)` 只写子区间；getFD/getChannel 非空。
- 边界: `write(int)` 低 8 位语义（配合长度断言）。
- 健壮性: 独立临时目录 + finally 清理；固定字节。
- 白盒 `testWhiteBox`: 继承 OutputStream、write(int)/getFD/getChannel 存在。

## 深化追加（契约级）

新增维度（全部经 JDK-27 实测锁定）：
- 构造负例矩阵：null File NPE 消息指名 "file"（27 特有 helpful message）；null String 与 (null,true)/(null,false) NPE；目录 FNFE 精确消息；缺失父目录 FNFE（File/String,bool）
- close 语义：double close 幂等；三种 write 关闭后 IOException("Stream Closed")；flush 关闭后不抛（空实现）；fd valid true→false；getFD 同实例
- write(int) 低 8 位矩阵：0x1FF→0xFF、0x141→0x41、0x100→0x00
- write(b,off,len) 参数矩阵与零写入验证
- append/truncate：全部四个构造变体（readAllBytes 回读）
- 可见性：写后长度即时可见；channel position 随写前进；经基类引用写
- 关闭后访问器：channel 关闭（isOpen false）、getFD/getChannel 仍可调用
- 白盒：输出流无 read/skip/available/readAllBytes 方法

负例矩阵：null NPE（消息指名 "file"）/ 目录 FNFE / 缺失父目录 FNFE / write(b,2,1) IOOBE / write(null) NPE / 关闭后 write IOException("Stream closed" 大写 C 的 "Stream Closed")。

版本差异记录：null 构造与 write(null) 的 NPE 消息 27 指名参数（8 为 null）；其余异常消息双侧一致；FOS 无 27 特有新 API。

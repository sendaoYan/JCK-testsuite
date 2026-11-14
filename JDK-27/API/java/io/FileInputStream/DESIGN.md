# java.io.FileInputStream 测试方案 (DESIGN)

## 1. API 功能
- 构造: `FileInputStream(String)`、`FileInputStream(File)`、`FileInputStream(FileDescriptor)`
- 读: `read()`、`read(byte[])`、`read(byte[],int,int)`、`readAllBytes()`、`readNBytes(int)`
- 定位/传输: `skip(long)`、`available()`、`transferTo(OutputStream)`
- 底层访问: `getFD()`、`getChannel()`、`close()`

## 2. Java 用户如何使用
- `try (FileInputStream fis = new FileInputStream(file)) { ... }` 读文件字节。
- 组合链路：`FileOutputStream` 写文件 → `FileInputStream` 逐步 read/skip/readNBytes → `readAllBytes` 全量 → `transferTo` 拷贝到内存流。

## 3. 开源代码实际用法调研（真实片段）
- `try (FileInputStream fs = new FileInputStream(release)) {` — `jdk25u-dev/test/jdk/tools/lib/tests/JImageValidator.java:148`
- `try (FileInputStream fin = new FileInputStream(cacerts.toFile())) {` — `jdk25u-dev/test/jdk/tools/jlink/runtimeImage/UpgradeableFileCacertsTest.java:125`
- `try (FileInputStream fis = new FileInputStream(inFile);` — `jdk25u-dev/test/jdk/tools/launcher/BigJar.java:91`

## 4. 测试用例设计 + 自动化实现（指向 FileInputStreamTest.java 黑盒 `testBlackBox`）
- 正常/组合: 写已知 7 字节 → read()/read(byte[],off,len)/skip/readNBytes 顺序读 → readAllBytes 全量 → transferTo 拷贝 → getFD/getChannel 非空。
- 边界: EOF `read()==-1`；打开不存在文件抛 `FileNotFoundException`。
- 健壮性: 独立 `Files.createTempDirectory("jck-fis")` + finally 清理；固定字节序列。
- 白盒 `testWhiteBox`: 继承 InputStream、readAllBytes/readNBytes/transferTo/getChannel 存在。

## 深化追加（契约级）

新增维度（全部经 JDK-27 实测锁定）：
- 构造负例矩阵：同 JDK-8 文案（缺失/目录/父目录），null File NPE 消息指名 "file"（Cannot invoke "java.io.File.isInvalid()"）
- close 语义：double close 幂等；read/available → IOException("Stream Closed")；**skip 关闭后 → IOException("Bad file descriptor")（与 JDK-8 的 "Stream Closed" 为锁定差异）**；getFD/getChannel 仍可调用；close 前获取的 channel 随流关闭
- FD 生命周期：open valid / close 后 invalid / 同实例
- read 参数矩阵：含 readNBytes(int) -1 → IAE、readNBytes(b,1,2) IOOBE、readNBytes(0) 空数组
- skip 契约：负数抛 IOException("Invalid argument")（同 8）；skip(100) 越 EOF 后 read=-1；空文件 skip(3)=3
- 大文件分块矩阵（30000 字节，跨内部缓冲区）：readAllBytes 端到端、readNBytes(5)+readNBytes(b,0,3)+skipNBytes(10)+transferTo 计数与内容、skipNBytes(精确长度) 后 read=-1、EOF 处 skipNBytes(1) **不抛 EOFException**（FIS 覆写 skip 的 lseek 越界成功返回 n，与基类默认实现不同）
- channel position 随读前进；File 与 String 构造一致

负例矩阵：缺失文件 FNFE / 目录 FNFE("<path> (<平台 OS 原因>)" 结构) / null File NPE("file") / read(b,1,2) IOOBE / readNBytes(-1) IAE / 关闭后 read/available IOException("Stream Closed")、skip IOException("Bad file descriptor")。

版本差异记录：skip 关闭后消息 8 "Stream Closed" vs 27 "Bad file descriptor"；基类 skipNBytes 的 EOFException 语义被 FIS 的 skip 覆写改变（EOF 处 skipNBytes 正数不抛）；null NPE 消息 27 指名参数。不测项：finalize；close 后懒创建 channel 的 isOpen；越过 EOF 后再小 skip 的返回值（非确定）。

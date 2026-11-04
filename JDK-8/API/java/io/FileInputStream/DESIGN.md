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

## 深化追加（契约级）

新增维度（全部经 JDK-8 实测锁定）：
- 构造负例矩阵：缺失文件 FNFE 消息含路径 + "(No such file or directory)"；目录 FNFE 消息精确 "<path> (Is a directory)"；null File/String NPE
- close 语义：double close 幂等；read/available 关闭后 IOException("Stream Closed")（大写 C）；skip 关闭后同为 "Stream Closed"；getFD/getChannel 关闭后仍可调用；close 前获取的 channel 随流关闭（isOpen false）
- FD 生命周期：open valid / close 后 invalid / getFD 同实例
- read 参数矩阵：(b,1,2)/(b,3,0)/(b,-1,1) IOOBE、null NPE、(b,b.length,0)=0、空数组 0；参数失败不移动位置
- skip 契约：skip(0)=0；**FIS 覆写 skip：负数抛 IOException("Invalid argument")（native lseek EINVAL），与基类「负数返回 0」不同**；skip(2) 精确；skip(100) 可越过 EOF（lseek 语义）随后 read=-1；空文件 skip(3)=3（同样越界成功）
- 空文件矩阵：read=-1/read(b)=-1/available=0
- channel position 随读前进；File 构造与 String 构造读回一致

负例矩阵：缺失文件 FNFE / 目录 FNFE / null File NPE / null String NPE / read(b,1,2) IOOBE / read(b,3,0) IOOBE / read(null) NPE / 关闭后 read/available/skip IOException。

版本差异（8 vs 27）：skip 关闭后消息 JDK-8 "Stream Closed" vs JDK-27 "Bad file descriptor"；null 构造 NPE 消息 27 侧指名 "file"；目录消息与缺失消息双侧同文案。JDK-27 另有 readAllBytes/readNBytes/transferTo/skipNBytes 的 FIS 覆写行为（见 27 侧 DESIGN）。

不测项：finalize（约定不测）；close 后懒创建 channel 的 isOpen 状态（实测非确定，两次运行结果不同，不作断言）；越过 EOF 后再做小 skip 的返回值（native 层非确定）。

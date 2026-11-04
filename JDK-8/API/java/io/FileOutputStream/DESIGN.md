# java.io.FileOutputStream 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 构造：`(File)`、`(File, boolean append)`、`(String path)`、`(String, boolean append)`、`(FileDescriptor)`。
- 写：`write(int)`、`write(byte[])`、`write(byte[],int,int)`。
- 资源：`getFD()`、`getChannel()`、`close()`。
- 语义：默认 append=false 截断；append=true 追加。

## 2. Java 用户如何使用
- `try (FileOutputStream fos = new FileOutputStream(f)) { fos.write(bytes); }` 写/覆盖文件；日志类场景用 `(f, true)` 追加。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `new FileOutputStream(f, true)` 追加 — `jdk8u-dev/jdk/src/share/classes/java/io/FileOutputStream.java`（append 构造）。
- 文件写回 — `jdk8u-dev/jdk/src/share/classes/java/util/` 多处工具（properties 落盘）。

## 4. 测试用例设计 + 自动化实现（FileOutputStreamTest.java，纯黑盒）
黑盒场景:
1. 写 3 字节 → 文件长度 3；getFD/getChannel 非空。
2. append=true 追加 → 长度累加 + 读回全量比对。
3. 默认模式截断 → 长度 1 + 内容精确。
4. write(byte[],off,len) 只写指定范围（长度+内容）。
5. String 构造 + append。
6. 目录作为目标 → FileNotFoundException。
- 健壮性: 临时目录 + finally 递归清理。

## 深化追加（契约级）

新增维度（全部经 JDK-8 实测锁定）：
- 构造负例矩阵：null File/String/(String,null,true)/(File,null,false) NPE；目录 FNFE 消息精确 "<path> (Is a directory)"（含 append 变体）；缺失父目录 FNFE 含路径 + "(No such file or directory)"（File/String/String,bool 三变体）
- close 语义：double close 幂等；write(int)/write(byte[])/write(b,off,len) 关闭后 IOException("Stream Closed")；**flush 关闭后不抛（FOS.flush 为空实现）**；fd.valid open true → close false；getFD 同实例
- write(int) 低 8 位矩阵：0x1FF→0xFF、0x141→0x41、0x100→0x00
- write(b,off,len) 参数矩阵：(b,2,1)/(b,-1,1)/(b,0,-1)/(b,3,0) IOOBE、null NPE、(b,b.length,0) 合法、空数组 no-op、失败检查零写入
- append 定位：三个构造变体（File,true / String,true）追加顺序 1,2,3,4；truncate：默认构造与 (File,false)/(String,false) 均覆盖截断（长写短写后长度=短）
- 写后可见性：不 close 文件长度即更新；回读需新开 FileInputStream
- channel position 随写前进；经 OutputStream 基类引用写

负例矩阵：null 四变体 NPE / 目录 FNFE / 缺失父目录 FNFE / write 越界三种 IOOBE / write(null) NPE / 关闭后 write IOException("Stream Closed")。

版本差异（8 vs 27）：null 构造 NPE 消息 27 侧指名 "file"（8 为 null）；write(null) 消息 27 指名 "b"；其余消息双侧一致（"Stream Closed"/"(Is a directory)"/"(No such file or directory)"）。JDK-8 侧另有白盒断言 nullOutputStream 不存在（属 OutputStream 目录）。

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

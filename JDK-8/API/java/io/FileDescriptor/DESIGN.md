# java.io.FileDescriptor 测试方案 (DESIGN)

## 1. API 功能
- 静态标准描述符：`in`、`out`、`err`（public static final）。
- 构造：`()`（无 OS 资源，invalid）。
- `valid()`（fd != -1）、`sync()`（fsync，非法描述符抛 SyncFailedException）。

## 2. Java 用户如何使用
- `FileDescriptor.out.sync()` 强制刷新标准输出；`new FileOutputStream(f).getFD()` 获取底层描述符做 fsync（持久化屏障）。

## 3. 开源代码实际用法调研
- `valid()/sync()/closeAll()` — `jdk8u-dev/jdk/src/solaris/classes/java/io/FileDescriptor.java`。
- 标准流 `in/out/err` 定义 — 同文件（`fd=0/1/2`）。

## 4. 测试用例设计 + 自动化实现（FileDescriptorTest.java，纯黑盒）
黑盒场景:
1. in/out/err 非空、两两互异、均 valid。
2. 裸构造 → valid()==false；不等同 stdin；hashCode 可调。
3. 裸构造 sync() → SyncFailedException。
4. RandomAccessFile.getFD() valid；write 后 sync() 成功；close 后（注意：closeAll 只置 closed 标志、不重置 fd，故不做 close 后 valid 断言）。
5. FileDescriptor.out.sync() 成功或 SyncFailedException 均接受（非 fsync 目标平台允许失败）。
6. equals 自反/null/裸实例互异；toString 非空。
- 健壮性: 无环境依赖；临时文件 finally 删除。

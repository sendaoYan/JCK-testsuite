# java.io.FileDescriptor 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 常量：`in`/`out`/`err` 三个标准流描述符（static final）。
- 构造：`()` 生成不绑定 OS 资源的无效描述符。
- `valid()`（是否绑定）、`sync()`（fsync；无效描述符→SyncFailedException）。

## 2. Java 用户如何使用
- RandomAccessFile/FileInputStream 的 getFD() 拿到 fd 后在 close 前 sync() 强制落盘（崩溃一致性）；标准流描述符做探测。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- valid/sync 实现 — `jdk25u-dev/src/java.base/share/classes/java/io/FileDescriptor.java:177,209,219`（sync0 native SyncFailedException）。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/FileDescriptor/`。

## 4. 测试用例设计 + 自动化实现（FileDescriptorTest.testBlackBox）
1. in/out/err 非 null、互异、valid()==true。
2. `new FileDescriptor()` 无绑定：valid()==false、不等 in。
3. 裸描述符 sync()→SyncFailedException。
4. RAF.getFD().valid()==true，写 1 字节后 sync() 成功（真文件、临时目录）。
5. stdout.sync() 成功或抛 SyncFailedException（管道目标平台差异二选一，均合法）。
6. equals 自反/与 null false/两个裸 fd 不等；toString 非空。
- 健壮性: 临时文件 finally 删除；不对 stdin 做任何操作。

产出：`FileDescriptorTest.java`（白盒 + 黑盒共 24 断言）。

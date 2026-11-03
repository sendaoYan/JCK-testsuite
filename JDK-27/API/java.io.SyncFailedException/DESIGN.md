# java.io.SyncFailedException 测试方案 (DESIGN)

## 1. API 功能
- `(String desc)`：`FileDescriptor.sync()` 无法保证缓冲已落盘时抛出。

## 2. Java 用户如何使用
- 事务日志/数据库类应用调用 `fd.sync()` 强制刷盘失败时捕获并降级或报错。

## 3. 开源代码实际用法调研
- 抛出点 — `jdk25u-dev/src/java.base/share/classes/java/io/FileDescriptor.java`（sync 的 JNI 失败包装）。
- 官方用例（对照成功路径）— `jdk25u-dev/test/jdk/java/io/FileDescriptor/`。

## 4. 测试用例设计 + 自动化实现（SyncFailedExceptionTest.java）
- 白盒: extends IOException、唯一 (String) 构造。
- 黑盒场景:
  1. 构造器 message。
  2. 真实触发：未绑定 fd 的 `new FileDescriptor().sync()` 抛出且带 reason。
  3. 对照：真实 RAF 文件写后 sync() 正常返回。
  4. 可按 IOException 捕获。
- 健壮性: 临时文件 finally 删除。

产出：`SyncFailedExceptionTest.java`（白盒 2 + 黑盒 6 断言）。

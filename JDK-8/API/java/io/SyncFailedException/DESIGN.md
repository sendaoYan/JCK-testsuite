# java.io.SyncFailedException 测试方案 (DESIGN)

## 1. API 功能
- 单构造器 `(String)`；继承 IOException；表示 FileDescriptor.sync 失败。

## 2. Java 用户如何使用
- 在需要将数据强制落盘时调用 fd.sync() 并捕获失败。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/SyncFailedException.java`。

## 4. 测试用例设计 + 自动化实现（SyncFailedExceptionTest.java，纯黑盒）
黑盒场景:
1. `(String)` 消息保留、无 cause。
2. 未绑定描述符 sync() 抛 SyncFailedException。
3. 真实打开文件描述符 sync() 成功（对比）。
4. 作为 IOException 捕获。
- 健壮性: 无反射白盒。

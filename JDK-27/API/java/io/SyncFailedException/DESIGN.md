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

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 38，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `SyncFailedException`：双侧同值 `-2353342684412443330L`
### 新增测试方法
1. testThrowableProtocol（全矩阵 + 非 ObjectStreamException 断言）
2. testSerializationContract（message/cause/suppressed 往返）
3. testTriggerPrecision（未绑定 fd 精确 message；FileDescriptor.in / 只读 RAFD / 追加 FOS sync 成功矩阵）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- new FileDescriptor().sync() → 「sync failed」（固定 message，无 errno）
- stdin fd / 只读文件 fd / 追加写 fd 的 sync() 在本平台成功（对照）

### JDK-8 vs JDK-27 差异
- 无（构造器面、message 文案、serialVersionUID、行为语义双侧一致）。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。

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

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 36，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
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

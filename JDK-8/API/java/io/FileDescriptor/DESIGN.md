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

## 深化追加（契约级）

新增维度（全部经 JDK-8 实测锁定）：
- 三静态字段：二次访问同一实例（static final 身份）、类型恰为 FileDescriptor
- toString 格式 "java.io.FileDescriptor@<hex>"；hashCode 同实例稳定
- bare 构造器：sync → SyncFailedException 精确消息 "sync failed"；与 stdin 不等；与无关对象不等
- RAF FD 生命周期：open valid、getFD 同实例、写后 sync 不抛、close 后 invalid、**closed fd 的 sync → SyncFailedException("sync failed")**
- FIS/FOS 的 FD：两流 FD 互不相等（equals false）、与 stdout 不等、close 后双双 invalid
- sync 幂等（活 fd 连续两次成功）；HashSet 身份包含；err.sync 与 out.sync 同样容错两分支
- 白盒：final 类、sync() 在 JDK-8 是 native（27 改非 native）、in 是 static final、无接口、直接子类 Object

负例矩阵：bare sync SyncFailedException("sync failed") / closed fd sync SyncFailedException("sync failed")。

版本差异（8 vs 27）：sync() 的 native 修饰（8 native / 27 非 native）；其余行为（valid 生命周期、"sync failed" 消息、身份语义）双侧一致。

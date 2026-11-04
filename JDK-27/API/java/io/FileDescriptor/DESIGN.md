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

## 深化追加（契约级）

新增维度（全部经 JDK-27 实测锁定）：
- 三静态字段身份与类型；toString 格式；hashCode 稳定
- bare 构造器：sync → SyncFailedException("sync failed")（与 8 同文案）
- RAF FD 生命周期：valid→invalid、getFD 同实例、closed fd sync 抛 "sync failed"
- FIS/FOS FD 互异与失效；sync 幂等；HashSet 身份；err.sync 容错
- 白盒：sync() 在 JDK-27 已非 native；无接口；直接子类 Object

负例矩阵：bare sync SyncFailedException("sync failed") / closed fd sync SyncFailedException("sync failed")。

版本差异记录：sync() 修饰符 8 native vs 27 非 native（反射断言各自锁定）；"sync failed" 消息与 valid 生命周期双侧一致（JDK-8 时代注释「close 不复位 fd」经实测已不成立：双侧 close 后 valid() 均为 false）。

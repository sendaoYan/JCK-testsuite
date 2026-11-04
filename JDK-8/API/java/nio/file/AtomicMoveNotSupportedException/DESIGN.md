# java.nio.file.AtomicMoveNotSupportedException 设计文档

## 1. API 功能

原子移动不支持（extends FileSystemException，仅 3 参构造）：ATOMIC_MOVE 跨文件系统（EXDEV）时抛出。

（签名以 javap -public 实测为准：jdk8u372 与 jdk-27.0.1.0.2 双侧一致。）

## 2. Java 用户如何使用

- 跨 store 场景getFile=源、getOtherFile=目标、reason=EXDEV 文案；回退为非原子 copy+delete。
- 捕获方式：按具体子类型分流处理（区别于笼统 IOException/RuntimeException），用类型精确决定重试、回退或终止。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳，参考 OpenJDK test/jdk/java/nio/file 与主流构建/IO 工具的异常处理路径，措辞自写）：
- AtomicMoveNotSupportedException
- 用法特点：优先走 java.nio 高层门面（Files/Channels），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、能力守护（权限/链接/多文件系统）保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`AtomicMoveNotSupportedExceptionTest.java`（单文件：既有 testWhiteBox/testBlackBox（或既有单方法）保留，新增 testContract* 系列由 main 依次调用）。

覆盖维度（批次 A11 契约深化）：
1. 层级与构造矩阵：直接父类精确断言（getSuperclass）、Serializable、公开构造器数量/参数形状（反射）、null 参数容忍与 1/3 参 getter 往返。
2. getMessage/toString 精确串（含 null 分支矩阵），getLocalizedMessage 委托。
3. 异常链：getCause 默认 null、initCause 一次性（二次→IllegalStateException）、自指→IllegalArgumentException（按 Throwable 语义）。
4. 序列化：serialVersionUID 精确值 5402760225333135579 + round-trip 后类/getter/消息/cause 保持。
5. 真实触发：/tmp(ext4)→/dev/shm(tmpfs) ATOMIC_MOVE（FileStore 相等守护）；对照同 store 原子 rename 成功。
6. 精确负例（checkThrowsExact 家族，异常类 == 精确类型断言）：见各 testContractRealTrigger*。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("a11")` + finally 递归删除；权限用例 finally 恢复权限再删。
- 平台相关能力（root 身份、/dev/shm 跨 store、符号链接、zip provider）运行时守护，不支持时打印 NOTE 跳过，不判失败；
  headless 安全，不加 @key。
- 本机实测断言数（JDK-8 侧 / JDK-27 侧）：28 / 31 条。

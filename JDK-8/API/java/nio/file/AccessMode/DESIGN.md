# java.nio.file.AccessMode 设计文档

## 1. API 功能

访问模式枚举 [READ, WRITE, EXECUTE]（FileSystemProvider.checkAccess 的参数）。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法：直接面向本类型编程，或经 Files/FileChannel/WatchService 等门面 API 间接触达。
- 组合用法：与本包其它类型配合（选项 + Files 操作、WatchService 注册监视）。
- 生命周期：获取 -> 使用 -> 关闭/释放；AutoCloseable 资源用 try-with-resources。
- 失败路径：通过本类型或配套异常类型精确报告失败（精确异常类型断言）。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 权限预检 / 选项组合 / 目录监视
- 用法特点：优先走 java.nio 高层门面（Files/Channels/WatchService），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、有界 poll/超时等待保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`AccessModeTest.java`（单文件，白盒 + 黑盒由 main 依次调用）。

设计：白盒校验 enum/3 常量/顺序；枚举矩阵（values 顺序/name/ordinal/compareTo/toString/declaringClass/valueOf 往返/valueOf 小写 IAE/valueOf(null) NPE/EnumSet）；黑盒经 provider.checkAccess 真实调用（rwx 全过、EXECUTE 对不可执行文件 AccessDeniedException、缺失 NoSuchFileException、null NPE）。

版本实测：JDK-8 与 JDK-27 行为完全一致。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 负例全部断言精确异常类型（IllegalArgumentException / NullPointerException /
  UnsupportedOperationException / IOException 子类等），不笼统 catch。
- WatchService 用例一律 poll(有界超时)，超时 fail with message，不挂死；辅助线程 daemon + join(超时)。
- headless 安全，不加 @key。

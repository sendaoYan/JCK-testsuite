# java.nio.file.OpenOption 设计文档

## 1. API 功能

标记接口（无方法）：newByteChannel/newInputStream/newOutputStream 的选项槽；实现者 StandardOpenOption、LinkOption。

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

文件：`OpenOptionTest.java`（单文件，白盒 + 黑盒由 main 依次调用）。

设计：白盒校验接口零声明方法；实现者归类（StandardOpenOption 十常量、LinkOption）；黑盒选项槽语义（open() 无选项默认 READ、空数组等价、未知选项 UnsupportedOperationException、null 元素 NPE、LinkOption 经 O_NOFOLLOW 对符号链接打开抛 IOException、对常规文件正常）。

null 元素 NPE 双侧实测一致。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 负例全部断言精确异常类型（IllegalArgumentException / NullPointerException /
  UnsupportedOperationException / IOException 子类等），不笼统 catch。
- WatchService 用例一律 poll(有界超时)，超时 fail with message，不挂死；辅助线程 daemon + join(超时)。
- headless 安全，不加 @key。

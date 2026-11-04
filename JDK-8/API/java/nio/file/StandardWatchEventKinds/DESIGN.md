# java.nio.file.StandardWatchEventKinds 设计文档

## 1. API 功能

final 工具类（私有构造）：四个标准事件种类 ENTRY_CREATE/ENTRY_DELETE/ENTRY_MODIFY/OVERFLOW。

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

文件：`StandardWatchEventKindsTest.java`（单文件，白盒 + 黑盒由 main 依次调用）。

设计：白盒（final/私有构造/字段数>=4/非 enum 非接口）；kind 矩阵（name 精确、entry 三者 type()==Path.class、OVERFLOW type()==Object.class、toString==name、引用相等矩阵、hashCode 稳定、四者入 HashSet size=4、同名自制 Kind 不等于常量）；黑盒实测 inotify：注册三事件→写新文件→有界 poll（5s，分批重试至 CREATE+MODIFY 均见到，失败带消息）→context==精确文件名且相对、count==1、reset→delete→ENTRY_DELETE 送达。

断言强度决策：ENTRY_CREATE/DELETE 必断言；ENTRY_MODIFY 断言（本机 ext4/inotify 实测稳定，注释标明，分批到达用有界重试吸收）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 负例全部断言精确异常类型（IllegalArgumentException / NullPointerException /
  UnsupportedOperationException / IOException 子类等），不笼统 catch。
- WatchService 用例一律 poll(有界超时)，超时 fail with message，不挂死；辅助线程 daemon + join(超时)。
- headless 安全，不加 @key。

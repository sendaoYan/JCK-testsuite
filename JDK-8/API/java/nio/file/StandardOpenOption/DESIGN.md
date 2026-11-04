# java.nio.file.StandardOpenOption 设计文档

## 1. API 功能

文件打开选项枚举 (implements OpenOption): READ/WRITE/APPEND/TRUNCATE_EXISTING/CREATE/CREATE_NEW/DELETE_ON_CLOSE/SPARSE/SYNC/DSYNC。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法: FileChannel.open/Files.newByteChannel 的 OpenOption 变参控制打开与写模式。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- StandardOpenOption
- 用法特点：优先走 java.nio 高层门面（Files/Channels/Selector），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`StandardOpenOptionTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验 enum/10 常量/实现 OpenOption；黑盒验证 CREATE_NEW 拒绝已存在、APPEND 尾部追加、TRUNCATE_EXISTING 清空、READ 通道拒写、WRITE 通道拒读、DELETE_ON_CLOSE 关闭即删、缺文件无 CREATE 抛 NoSuchFileException。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 平台相关能力（权限/符号链接/watch 事件）以运行时能力守护，不支持时打印 NOTE 跳过，不判失败；
  headless 安全，不加 @key。
- 本机实测断言数：23 条。

### 第 4 部分追加（批次 A10 契约深化）

新增：全量 10 常量矩阵（顺序 [READ,WRITE,APPEND,TRUNCATE_EXISTING,CREATE,CREATE_NEW,DELETE_ON_CLOSE,SPARSE,SYNC,DSYNC] 双侧实测/ordinal/valueOf 往返/compareTo 链/EnumSet）；组合矩阵实测（READ+APPEND→IAE、WRITE+APPEND+TRUNCATE_EXISTING→IAE、APPEND 单独合法且隐含 WRITE、TRUNCATE_EXISTING 单独不截断、CREATE 打开既有不截断、CREATE_NEW+SPARSE 建 0 字节、SYNC/DSYNC 打开写成功）。

- 本机实测断言数（追加后）：见当前 `PASS (N assertions)` 输出。

# java.nio.file.FileStore 设计文档

## 1. API 功能

文件存储抽象类: name/type/isReadOnly/getTotalSpace/getUsableSpace/getUnallocatedSpace/supportsFileAttributeView/getFileStoreAttributeView/getAttribute。注: JDK-8 无 getBlockSize() (JDK-9+)。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法: Files.getFileStore(Path) 获取所在存储 -> 查询容量/只读/属性视图支持。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- FileStore
- 用法特点：优先走 java.nio 高层门面（Files/Channels/Selector），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`FileStoreTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验 abstract/无接口/9 个方法存在；黑盒验证 name/type 非空、容量自洽 (usable/unallocated <= total)、基本/owner 视图支持、getAttribute 与类型 getter 一致、未知属性名拒绝。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 平台相关能力（权限/符号链接/watch 事件）以运行时能力守护，不支持时打印 NOTE 跳过，不判失败；
  headless 安全，不加 @key。
- 本机实测断言数：25 条。

# java.nio.file.LinkOption 设计文档

## 1. API 功能

链接处理选项枚举 (implements OpenOption+CopyOption): NOFOLLOW_LINKS。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法: Files.exists/readAttributes 等 LinkOption 变参控制是否跟随符号链接。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- LinkOption
- 用法特点：优先走 java.nio 高层门面（Files/Channels/Selector），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`LinkOptionTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验 enum/1 常量/实现 OpenOption+CopyOption；黑盒验证默认跟随链接、NOFOLLOW 暴露链接本身、悬空链接跟随不存在/不跟随存在、readAttributes 两态、NOFOLLOW copy 保留链接。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 平台相关能力（权限/符号链接/watch 事件）以运行时能力守护，不支持时打印 NOTE 跳过，不判失败；
  headless 安全，不加 @key。
- 本机实测断言数：15 条。

### 第 4 部分追加（批次 A10 契约深化）

新增：完整枚举矩阵（values 恰 [NOFOLLOW_LINKS]/ordinal 0/compareTo 自身 0/toString==name/declaringClass/EnumSet allOf·noneOf/valueOf 往返/valueOf 小写 IAE/hashCode 稳定）；跟随语义扩展（空 LinkOption[] 等价默认跟随、notExists 同步、isRegularFile 跟随 true/isDirectory false、size 跟随取目标、delete 仅删链接目标存活、默认 copy 复制目标内容 vs NOFOLLOW 复制链接、getAttribute basic:isSymbolicLink 两态）。

- 本机实测断言数（追加后）：见当前 `PASS (N assertions)` 输出。

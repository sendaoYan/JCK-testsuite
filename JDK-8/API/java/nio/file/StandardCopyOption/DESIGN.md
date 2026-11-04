# java.nio.file.StandardCopyOption 设计文档

## 1. API 功能

复制/移动选项枚举 (implements CopyOption): REPLACE_EXISTING/COPY_ATTRIBUTES/ATOMIC_MOVE。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法: Files.copy/Files.move 的 CopyOption 变参控制覆盖/属性复制/原子移动。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- StandardCopyOption
- 用法特点：优先走 java.nio 高层门面（Files/Channels/Selector），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`StandardCopyOptionTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验 enum/3 常量/实现 CopyOption；黑盒验证普通 copy 拒绝覆盖、REPLACE_EXISTING 覆盖、COPY_ATTRIBUTES 复制 mtime、ATOMIC_MOVE 同存储移动且源消失。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 平台相关能力（权限/符号链接/watch 事件）以运行时能力守护，不支持时打印 NOTE 跳过，不判失败；
  headless 安全，不加 @key。
- 本机实测断言数：15 条。

### 第 4 部分追加（批次 A10 契约深化）

新增：精确枚举矩阵（values 恰 [REPLACE_EXISTING,COPY_ATTRIBUTES,ATOMIC_MOVE] 双侧实测顺序/ordinal/name/compareTo 链/toString/valueOf 全量往返/EnumSet/valueOf 小写 IAE）；选项作用域实测（move+REPLACE_EXISTING 覆盖既有文件、copy+ATOMIC_MOVE→UOE、move+COPY_ATTRIBUTES→UOE、空目录 copy 无选项建目标/REPLACE 覆盖空目录、目录 ATOMIC_MOVE 同存储成功 ext4、COPY_ATTRIBUTES mtime 毫秒级精确保持）。

- 本机实测断言数（追加后）：见当前 `PASS (N assertions)` 输出。

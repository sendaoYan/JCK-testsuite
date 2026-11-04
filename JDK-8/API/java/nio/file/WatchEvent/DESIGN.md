# java.nio.file.WatchEvent 设计文档

## 1. API 功能

事件接口&lt;T&gt;：kind() 返回 WatchEvent.Kind、count() 返回累计次数、context() 返回事件上下文（entry 事件为相对目录的 Path）；嵌套 Kind/Modifier 接口。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法：WatchKey.pollEvents() 取 List&lt;WatchEvent&lt;?&gt;&gt; 逐个处理 kind/context/count。
- 语义要点：事件取出即消费（二次 pollEvents 空）；count 为同 kind 合并计数。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 目录变更消费循环（take → pollEvents → reset）
- 用法特点：按 kind() 分派，context() 解析出相对路径再 resolve；测试侧断言事件序列与上下文名。

## 4. 测试用例设计 + 自动化实现

文件：`WatchEventTest.java`（白盒 + 黑盒由 main 依次调用）。

设计：白盒（接口/泛型参数名 T/kind·count·context 返回型/嵌套 Kind·Modifier 接口）；黑盒 inotify 实测：写新文件→有界 poll（分批重试至 CREATE+MODIFY 均见）→逐事件断言 kind 非 null、count>=1、CREATE 时 count==1、context 为 Path 且 == 精确文件名、相对（非绝对）、dir.resolve(context) 定位原文件、事件取出即消费（二次空）、reset 后 delete→ENTRY_DELETE context 精确。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 临时目录 + finally 清理；poll 一律有界；headless 安全，不加 @key。
- 断言强度决策：CREATE/DELETE 必断言；MODIFY 断言（ext4/inotify 实测稳定，注释标明）。

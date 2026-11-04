# java.nio.file.Watchable 设计文档

## 1. API 功能

可监视对象接口：register(WatchService, Kind...) 与 register(WatchService, Kind[], Modifier...) 两形式；Path 是平台实现。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法：Path.register(watchService, ENTRY_CREATE, ...) 获得WatchKey。
- 失败路径：空事件数组 IllegalArgumentException、null 数组 NPE、目录缺失 NoSuchFileException、非目录 NotDirectoryException、服务已关 ClosedWatchServiceException。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 目录监视（配置热加载、构建工具增量编译）
- 用法特点：Path.register 为主入口；测试侧临时目录 + finally 清理 + 有界 poll。

## 4. 测试用例设计 + 自动化实现

文件：`WatchableTest.java`（白盒 + 黑盒由 main 依次调用）。

设计：白盒反射校验两 register 重载的精确参数形态（Kind[] varargs / Kind[]+Modifier[]）与 WatchKey 返回型、Path implements Watchable；黑盒经接口引用两形式注册（key 有效、watchable()==目录、事件送达且 context 精确）、自制 Watchable 记录参数透传（varargs 到达为 Kind[]、modifier 数组原样、返回值 null 透传）、负例矩阵（无事件 IAE、null 事件 NPE、缺失目录 NoSuchFileException、注册普通文件 NotDirectoryException）。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 临时目录 + finally 清理；poll 一律有界（5s），超时 fail with message。
- 负例全部精确异常类型；headless 安全，不加 @key。
- 版本实测：JDK-8 与 JDK-27 行为完全一致。

# java.nio.file.WatchEvent.Kind 设计文档

## 1. API 功能

事件种类接口&lt;T&gt;：name() 识别名、type() 上下文类型；标准实现为 StandardWatchEventKinds 四常量（单例）。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法：注册时传 Kind，消费时 e.kind().name() 分派。
- 语义要点：相等性为引用比较（单例常量），同名自制 Kind 不等于常量。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- 按 name() 字符串分派处理逻辑
- 用法特点：默认 provider 拒绝非标准 kind（UnsupportedOperationException，消息即 kind 名）。

## 4. 测试用例设计 + 自动化实现

文件：`KindTest.java`（白盒 + 黑盒由 main 依次调用）。

设计：白盒（接口/泛型 T/name 返回 String/type 返回 Class）；标准四 kind 矩阵（name 精确、entry 三者 type Path、OVERFLOW type Object 实测、toString==name、单例恒等）；自制 Kind 同名不等于常量；黑盒：注册非标准 kind → UnsupportedOperationException 且消息==kind 名（实测）；标准 kind 注册后真实事件 kind().name()=="ENTRY_CREATE"。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 临时目录 + finally 清理；poll 有界；headless 安全，不加 @key。

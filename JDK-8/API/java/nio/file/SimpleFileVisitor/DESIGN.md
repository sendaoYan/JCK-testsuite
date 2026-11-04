# java.nio.file.SimpleFileVisitor 设计文档

## 1. API 功能

FileVisitor 的默认实现类 (具体类、受保护构造): 各回调默认 CONTINUE，visitFileFailed 原样重抛 IOException。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法: 匿名子类仅覆盖关心的回调，其余走默认；直接用于 walkFileTree。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- SimpleFileVisitor
- 用法特点：优先走 java.nio 高层门面（Files/Channels/Selector），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`SimpleFileVisitorTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验具体类/实现 FileVisitor/泛型/受保护默认构造；黑盒验证默认回调返回 CONTINUE、visitFileFailed 重抛同一异常、walk 覆盖文件、无权限目录经默认钩子抛 IOException。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 平台相关能力（权限/符号链接/watch 事件）以运行时能力守护，不支持时打印 NOTE 跳过，不判失败；
  headless 安全，不加 @key。
- 本机实测断言数：10 条。

## 深化追加（契约级，批次 A9）

新增维度（断言 11 → 33）：白盒：恰 4 个声明方法（为 FileVisitor 各方法提供默认实现）、全 public、
无声明字段、protected 无参构造。实测修正：默认实现四个方法对 file/dir 与 attrs 参数均
requireNonNull -> NullPointerException（任务书假设"忽略 null attrs"被运行结果推翻，改为断言精确
NPE）；postVisitDirectory(dir, ioe) 按身份重抛同一 IOException 对象；子类 super.visitFileFailed
先观察后重抛；SimpleFileVisitor<Object> 泛型实例化接受任意 T；全遍历计数
（2 pre / 3 file / 2 post）；裸子类可完整走平坦树。

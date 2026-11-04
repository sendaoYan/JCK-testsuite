# java.nio.file.FileVisitor 设计文档

## 1. API 功能

文件遍历访问器接口: preVisitDirectory/visitFile/visitFileFailed/postVisitDirectory。

（签名以 jdk8u372 javap -public 实测为准。）

## 2. Java 用户如何使用

- 常规用法: 实现四回调交给 Files.walkFileTree 驱动，形成 pre(dir)->file->post(dir) 生命周期。

## 3. 开源代码实际用法调研

典型开源使用方式（按社区常见形态归纳）：
- FileVisitor
- 用法特点：优先走 java.nio 高层门面（Files/Channels/Selector），仅在需要精细控制时直接触碰本类型；
  测试侧普遍以临时目录 + finally 递归清理、本地回环端口 + 守护线程保证确定性与无残留。

## 4. 测试用例设计 + 自动化实现

文件：`FileVisitorTest.java`（单文件，含 testWhiteBox() 与 testBlackBox()，由 main 依次调用）。

设计：白盒校验接口/泛型/四方法；黑盒验证两层级树六次回调顺序、子目录 pre 先于 post、无权限目录触发 visitFileFailed、访问器异常向上传播。

自动化与健壮性：
- 输出 `PASS (N assertions)` 计数断言；失败抛 AssertionError 立即定位。
- 文件类用例全部 `Files.createTempDirectory("jck")` + finally 递归删除。
- 平台相关能力（权限/符号链接/watch 事件）以运行时能力守护，不支持时打印 NOTE 跳过，不判失败；
  headless 安全，不加 @key。
- 本机实测断言数：13 条。

## 深化追加（契约级，批次 A9）

新增维度（断言 11 → 35）：四方法精确签名（参数类型/返回类型/throws IOException）反射断言；
非 @FunctionalInterface；无默认方法。行为：attributes 参数非 null 且 isDirectory/isRegularFile
与条目种类一致；postVisitDirectory 正常流 ioe == null；嵌套顺序 pre:sub < file:f2 < post:sub；
非存在起点 -> 单次 visitFileFailed 且异常为精确 NoSuchFileException、路径为起点本身；
visitFile 返回 null -> NPE（探针证实）；RuntimeException/IOException 从回调原样传播不包装。

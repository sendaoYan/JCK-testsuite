# java.nio.file.FileVisitOption 测试方案

## 1. API 功能
java.nio.file.FileVisitOption 是遍历选项枚举，JDK-8 恰 1 个常量:
- FOLLOW_LINKS: walkFileTree 遍历时跟随符号链接 (读取目标属性并递归进入)。
- 无 marker 接口 (不实现 OpenOption/CopyOption)。

## 2. Java 用户如何使用
- 常规用法: Files.walkFileTree(start, EnumSet.of(FileVisitOption.FOLLOW_LINKS), maxDepth, visitor)。
- 默认 (不传): 符号链接按链接本身访问 (visitFile + isSymbolicLink)，目录链接不进入。

## 3. 开源代码实际用法调研
- 通用形态: 目录树遍历器默认不跟随链接以避免环；需要"所见即目标"语义时显式传 FOLLOW_LINKS。

## 4. 测试用例设计 + 自动化实现
文件: `FileVisitOptionTest.java` (testWhiteBox + testEnumContract + testBehavior，main 依次调用)。

白盒 (反射): isEnum；顶层类型；恰 1 常量 FOLLOW_LINKS；不实现 OpenOption/CopyOption；
  extends java.lang.Enum；单 public 常量字段。

枚举契约: values() 恰 1 元素且每次新数组 (可涂写不影响枚举)；ordinal==0；toString==name；
  compareTo 自身 0；valueOf 往返同一性；负例精确类型: valueOf(小写) -> IAE、
  valueOf(未知) -> IAE、valueOf(null) -> NPE；EnumSet of/noneOf/allOf。

行为 (batch A9 探针 /tmp/nio-a9-probe/Probe7 双 JDK 实测):
- 目录符号链接: 默认 visitFile(isSymbolicLink=true) 且无 preVisitDirectory；
  FOLLOW_LINKS 下 preVisitDirectory(链接) 且目标内容被再次访问。
- 自环符号链接: FOLLOW_LINKS -> FileSystemLoopException (精确)；默认不跟随 -> 正常完成。
- 断链: FOLLOW_LINKS 下仍走 visitFile (lstat 回退) 且永不进 visitFileFailed；
  默认下 visitFile(isSymbolicLink=true)。

健壮性: 临时目录 + finally 递归删除 (含符号链接)；纯 headless 不加 @key。
本机实测断言数: 29 条 (旧 4)。

# java.nio.file.DirectoryStream 测试方案

## 1. API 功能
java.nio.file.DirectoryStream<T> 是目录条目流接口，extends Closeable, Iterable<T>；嵌套接口
Filter<T> (accept(T) throws IOException)。
JDK-8 用法 (以 jdk8u372 javap 实测为准)：
- 构造源: Files.newDirectoryStream(dir)/newDirectoryStream(dir, glob)/newDirectoryStream(dir, Filter)
- 迭代: iterator() (每流一次)；close()
- 注: 无 LinkOption 变体重载；glob 语法同 PathMatcher "glob:"。

## 2. Java 用户如何使用
- 常规用法: try-with-resources 打开流 -> for-each 遍历条目 (条目为 dir 拼接后的路径) -> close。
- 过滤: glob 字符串或 Filter lambda；Filter 抛 IOException 被 wrap 为 DirectoryIteratorException
  (cause 同对象)，RuntimeException 原样传播 (实测)。
- 组合用法: 与 Files.walkFileTree/Files.list 配合做目录扫描。

## 3. 开源代码实际用法调研
- 通用形态: 目录扫描器普遍用 try(DirectoryStream) + for-each；依赖"每流一个 iterator"、
  "条目父目录即被扫描目录"两个不变量；Filter 用于按后缀/属性筛选。

## 4. 测试用例设计 + 自动化实现
文件: `DirectoryStreamTest.java` (testWhiteBox + testBlackBox + testDeepened，main 依次调用)。

白盒 (反射): 接口；extends Closeable/Iterable；单类型参数；hasMethod iterator/close/Filter.accept。

黑盒 (行为): 列出全部条目恰一次且无 "."/".."；glob 大小写敏感；Filter lambda 选择集；
  close 幂等；条目 parent == dir。

深化 (batch A9，探针 /tmp/nio-a9-probe 实测):
- Filter 抛 IOException -> DirectoryIteratorException 且 cause 为同一对象；
  Filter 抛 RuntimeException 原样传播 (不包装)。
- close 后: 新 iterator() -> IllegalStateException；已取出的 iterator.next() ->
  NoSuchElementException (实测)；二次 iterator() -> IllegalStateException。
- iterator.remove -> UnsupportedOperationException；close 幂等。
- 负例精确类型: 对普通文件 newDirectoryStream -> NotDirectoryException；对不存在 ->
  NoSuchFileException；null -> NPE。
- glob "*.{txt,bin}" 大括号备选；条目大小写保留 (c.TXT 不被 *.txt 选中)。

健壮性: 临时目录 createTempDirectory + finally 递归删除；纯 headless 不加 @key。
本机实测断言数: 27 条 (旧 2)。

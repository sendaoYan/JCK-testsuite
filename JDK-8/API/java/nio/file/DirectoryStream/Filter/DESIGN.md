# java.nio.file.DirectoryStream.Filter 测试方案

## 1. API 功能
java.nio.file.DirectoryStream.Filter<T> 是嵌套函数式接口: boolean accept(T entry) throws IOException。
JDK-8 (以 jdk8u372 javap 实测为准)：
- 单抽象方法 accept；@FunctionalInterface (lambda/方法引用目标)。
- 仅作为 Files.newDirectoryStream(dir, Filter) 的参数出现。

## 2. Java 用户如何使用
- 常规用法: Files.newDirectoryStream(dir, p -> p.toString().endsWith(".txt")) 按谓词筛选。
- 组合用法: 与属性判断组合 (Files.isDirectory(entry))；独立调用 accept 做纯谓词。

## 3. 开源代码实际用法调研
- 通用形态: 目录过滤谓词；开源代码多以 lambda 一行实现，异常向上委托给流的迭代协议。

## 4. 测试用例设计 + 自动化实现
文件: `FilterTest.java` (testWhiteBox + testBlackBox，main 依次调用)。

白盒 (反射): 嵌套于 DirectoryStream；接口；单类型参数 T；accept 声明 throws IOException；
  @FunctionalInterface；单抽象方法。

黑盒 (行为，batch A9 全量重写+探针实测):
- 独立谓词: 匿名类与 lambda 两种形态；accept 精确选择 (.txt 后缀非子串)；绝对路径可用。
- 流内行为: 精确选择集 {a.txt,c.txt}；accept 对每个候选条目恰调用一次 (4 条目=4 次)；
  accept 收到的条目 parent == 被扫描目录。
- 异常契约 (实测): accept 抛 IOException -> 迭代侧 DirectoryIteratorException 且 cause 为
  同一对象；accept 抛 RuntimeException 原样传播 (不包装)。
- DirectoryIteratorException 形状: 是 RuntimeException；构造器存 cause；消息含 cause 消息。

健壮性: 临时目录 + finally 递归删除；纯 headless 不加 @key。
本机实测断言数: 25 条 (旧 4)。

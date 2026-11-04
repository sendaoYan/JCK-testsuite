# java.nio.file.Paths 测试方案

## 1. API 功能
java.nio.file.Paths 是构造 Path 的静态工厂类 (final class, extends Object)。
JDK-8 方法 (以 jdk8u372 javap -public 实测为准)：
- get(String first, String... more): 拼接分量构造路径
- get(URI): 从 URI (file:// 等) 构造路径

## 2. Java 用户如何使用
- Paths.get("a","b","c") 多参拼接；Paths.get("a/b/c") 单参带分隔符；Paths.get(URI.create(...))
  从 URI 构造绝对路径。

## 3. 开源代码实际用法调研
- 通用形态: Paths.get 是 JDK-8 时代唯一的标准路径工厂 (JDK-11 后才有 Path.of)，代码库普遍使用。

## 4. 测试用例设计 + 自动化实现
文件: `PathsTest.java` (单文件，testWhiteBox() + testBlackBox()，由 main 依次调用)。

白盒 (反射): final class/extends Object；hasMethod 校验 get(String,String...)/get(URI)。

黑盒 (行为): Paths.get("a","b","c") 分量 -> 多参 get 与单参 joined 等价 -> 重复 get 相等 ->
joined toString -> 空串路径 toString 为空 -> get(URI) 绝对路径与路径串 -> get((String)null) 与
get((URI)null) 抛 NullPointerException。

健壮性: 纯内存路径运算，无文件/网络/线程依赖；不依赖墙钟/随机；headless 安全不加 @key。
本机实测断言数: 15 条。

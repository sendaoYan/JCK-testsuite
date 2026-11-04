# java.nio.file.Paths 测试方案

## 1. API 功能
java.nio.file.Paths 是构造 Path 的静态工厂类 (final class, extends Object)。
方法 (以 javap -public 实测为准)：
- get(String first, String... more): 拼接分量构造路径
- get(URI): 从 URI (file:// 等) 构造路径
JDK-11+ 另新增等价工厂 Path.of，本测试同时断言 Paths.get 与 Path.of 等价。

## 2. Java 用户如何使用
- Paths.get("a","b","c") 多参拼接；Paths.get("a/b/c") 单参带分隔符；Paths.get(URI.create(...))
  从 URI 构造绝对路径；与 Path.of 等价。

## 3. 开源代码实际用法调研
- 通用形态: JDK-11 前代码库普遍使用 Paths.get；新代码偏好 Path.of，两者行为等价。

## 4. 测试用例设计 + 自动化实现
文件: `PathsTest.java` (单文件，testWhiteBox() + testBlackBox()，由 main 依次调用)。

白盒 (反射): final class/extends Object；hasMethod 校验 get(String,String...)/get(URI)。

黑盒 (行为): Paths.get("a","b","c") 分量 -> 多参 get 与单参 joined 等价 -> Paths.get 与 Path.of
等价 -> 空串路径 toString 为空 -> get(URI) 绝对路径与路径串。

健壮性: 纯内存路径运算，无文件/网络/线程依赖；不依赖墙钟/随机；headless 安全不加 @key。
本机实测断言数: 12 条。

## 深化追加（契约级，批次 A9）

新增维度（断言 11 → 42）：多段拼接矩阵、NUL -> InvalidPathException（getIndex()==-1）、null 矩阵；
file URI 矩阵与负例精确类型（file 相对 URI -> IllegalArgumentException，jar/bogus/http ->
FileSystemNotFoundException）；Path.of(URI) 镜像断言（版本注释：Path.of 为 JDK-11+）。

# java.nio.file.Path 测试方案

## 1. API 功能
java.nio.file.Path 是文件系统路径抽象接口，extends Comparable<Path>, Iterable<Path>, Watchable。
JDK-8 方法 (以 jdk8u372 javap -public 实测为准)：
- 系统/判断: getFileSystem/isAbsolute
- 分量: getRoot/getFileName/getParent/getNameCount/getName(int)/subpath(int,int)
- 前缀/后缀: startsWith(Path|String)/endsWith(Path|String)
- 组合: normalize/resolve(Path|String)/resolveSibling(Path|String)/relativize(Path)
- 转换: toUri/toAbsolutePath/toRealPath(LinkOption...)/toFile
- 注册: register(WatchService,Kind<?>...[,Modifier...])
- 迭代/比较: iterator/compareTo/equals/hashCode/toString
- 注: JDK-8 无 Path.of 静态工厂 (JDK-11+)，构造用 Paths.get。

## 2. Java 用户如何使用
- 常规用法: Paths.get("/a/b/c") 或 Paths.get("a","b","c") 构造 -> getFileName/getParent/getName/
  subpath 访问分量 -> resolve/resolveSibling/relativize/normalize 组合 -> toFile/toUri 互操作。
- 组合用法: 与 Files (读写/属性) 及 WatchService (register) 组合。

## 3. 开源代码实际用法调研
- 通用形态: Path 作为路径值对象广泛使用，配合 Files 与 Stream 遍历；测试侧用相对/绝对路径
  分量断言而非依赖平台默认分隔符 (本套件在 Linux 下以 "/" 为分隔符)。

## 4. 测试用例设计 + 自动化实现
文件: `PathTest.java` (单文件，testWhiteBox() + testBlackBox()，由 main 依次调用)。

白盒 (反射): 接口/abstract/无父类；hasInterface 校验 Comparable/Iterable/Watchable；hasMethod
校验 17 个方法 (getFileName/getName/subpath/startsWith/endsWith/resolve/resolveSibling/relativize/
normalize/toUri/toAbsolutePath/toRealPath/toFile/register 等)。

黑盒 (行为): Paths.get("/a/b/c") 分量访问 -> startsWith/endsWith (Path 与 String 两形态) ->
normalize 折叠冗余分隔符 -> resolve/resolveSibling (String 与 Path 两形态) -> relativize (子路径/
上跳) -> iterator 遍历分量 -> toAbsolutePath/toUri/toFile -> Paths.get(URI) 与绝对路径等价 ->
equals/hashCode/compareTo -> subpath 越界与 getName(-1) 抛 IllegalArgumentException。

健壮性: 纯内存路径运算，无文件/网络/线程依赖；不依赖墙钟/随机；headless 安全不加 @key。
本机实测断言数: 50 条。

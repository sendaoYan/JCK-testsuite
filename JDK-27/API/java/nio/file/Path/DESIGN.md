# java.nio.file.Path 测试方案

## 1. API 功能
java.nio.file.Path 是文件系统路径抽象接口，extends Comparable<Path>, Iterable<Path>, Watchable。
JDK-27 在 JDK-8 基础上新增静态工厂 Path.of(String,String...) 与 Path.of(URI) (JDK-11+)。
方法: getFileSystem/isAbsolute；分量 getRoot/getFileName/getParent/getNameCount/getName/subpath；
startsWith/endsWith (Path|String)；normalize/resolve/resolveSibling/relativize；
toUri/toAbsolutePath/toRealPath/toFile；register(WatchService,Kind...[,Modifier...])；
iterator/compareTo/equals/hashCode/toString。

## 2. Java 用户如何使用
- 常规用法: Path.of("/a/b/c") 或 Path.of("a","b","c") 构造 -> getFileName/getParent/getName/
  subpath 访问分量 -> startsWith/endsWith -> normalize/resolve/resolveSibling/relativize ->
  toAbsolutePath/toFile/toUri 互操作。
- 组合用法: 与 Files (读写/属性) 及 WatchService (register) 组合。

## 3. 开源代码实际用法调研
- 通用形态: Path 作为路径值对象广泛使用，配合 Files 与 Stream 遍历；测试侧用分量断言而非
  依赖平台默认分隔符 (本套件在 Linux 下以 "/" 为分隔符)。

## 4. 测试用例设计 + 自动化实现
文件: `PathTest.java` (单文件，testWhiteBox() + testBlackBox()，由 main 依次调用)。

白盒 (反射): 接口/abstract/无父类；hasInterface 校验 Comparable/Iterable/Watchable；hasMethod
校验 of/getFileName/resolve/relativize/toFile 等。

黑盒 (行为): Path.of("/a/b/c") 分量访问 -> startsWith/endsWith -> normalize 折叠冗余分隔符 ->
resolve/resolveSibling (String 与 Path) -> relativize (子路径/上跳) -> iterator 遍历分量 ->
toAbsolutePath/toFile -> Path.of(URI) 与绝对路径等价 -> equals/hashCode -> subpath 越界 IAE。

健壮性: 纯内存路径运算，无文件/网络/线程依赖；不依赖墙钟/随机；headless 安全不加 @key。
本机实测断言数: 34 条。

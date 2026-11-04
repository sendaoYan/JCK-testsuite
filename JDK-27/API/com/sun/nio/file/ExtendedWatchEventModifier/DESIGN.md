# com.sun.nio.file.ExtendedWatchEventModifier 测试设计

## 1. API 功能
- 类型：`public enum ExtendedWatchEventModifier implements java.nio.file.WatchEvent.Modifier`（模块 `jdk.unsupported`）。
- 枚举常量：`FILE_TREE`（注册整棵文件树而非单个目录，某些平台支持）。
- 方法：`values()`、`valueOf(String)` 及 `Enum` 继承方法；`WatchEvent.Modifier` 为标记接口。

## 2. Java 用户如何使用
```java
import java.nio.file.*;
import static com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE;
WatchService ws = FileSystems.getDefault().newWatchService();
Path top = Path.of("dir");
top.register(ws, new WatchEvent.Kind<?>[]{ StandardWatchEventKinds.ENTRY_CREATE }, FILE_TREE);
```

## 3. 开源代码实际用法调研
- JDK 源码定义：`~/git/jdk25u-dev/src/jdk.unsupported/share/classes/com/sun/nio/file/ExtendedWatchEventModifier.java`，常量 `FILE_TREE(FileSystemOption.FILE_TREE)`。
- JDK 自带测试：`~/git/jdk25u-dev/test/jdk/java/nio/file/WatchService/FileTreeModifier.java`（`@requires os.family == "windows"`，`@modules jdk.unsupported`），用 `FILE_TREE` 注册目录树并断言深层子目录事件。
- 第三方框架基本不直接用该平台专有修饰符，行为权威以 JDK 源码 + test/jdk 为准。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`ExtendedWatchEventModifierTest.testBlackBox()`）覆盖：
- `FILE_TREE` 的 `name()/toString()/ordinal()==0/getDeclaringClass()`、`compareTo` 自比较、`equals`。
- `values()` 每次返回新数组但内容一致。
- `valueOf("FILE_TREE")` 恒等；`valueOf("FILE")` 抛 `IllegalArgumentException`；`valueOf(null)` 抛 `NullPointerException`。
白盒部分补齐：isEnum/final/extends Enum/implements WatchEvent.Modifier/字段 FILE_TREE/单常量。

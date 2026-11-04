# com.sun.nio.file.ExtendedOpenOption 测试设计

## 1. API 功能
- 类型：`public enum ExtendedOpenOption implements java.nio.file.OpenOption`（模块 `jdk.unsupported`）。
- 枚举常量（声明序）：`NOSHARE_READ`、`NOSHARE_WRITE`、`NOSHARE_DELETE`、`DIRECT`（`DIRECT` 为 JDK 10 引入，要求直接 I/O，不支持时打开文件抛 `UnsupportedOperationException`）。
- 方法：`values()`、`valueOf(String)` 及 `Enum` 继承方法。
- `OpenOption` 为标记接口。

## 2. Java 用户如何使用
```java
import java.nio.channels.FileChannel;
import java.nio.file.*;
import com.sun.nio.file.ExtendedOpenOption;
FileChannel ch = FileChannel.open(path, StandardOpenOption.WRITE, ExtendedOpenOption.DIRECT);
// 或
for (ExtendedOpenOption o : ExtendedOpenOption.values()) { ... }
ExtendedOpenOption o = ExtendedOpenOption.valueOf("NOSHARE_READ");
```

## 3. 开源代码实际用法调研
- JDK 源码定义：`~/git/jdk25u-dev/src/jdk.unsupported/share/classes/com/sun/nio/file/ExtendedOpenOption.java`，四个常量均注册到 `jdk.internal.misc.FileSystemOption`。
- JDK 自带测试：`~/git/jdk25u-dev/test/jdk/java/nio/channels/FileChannel/directio/DirectIOTest.java`、`ReadDirect.java`、`WriteDirect.java`、`PreadDirect.java`、`PwriteDirect.java` 使用 `ExtendedOpenOption.DIRECT` 验证直接 I/O 路径。
- 第三方框架很少直接依赖该 JDK 专有选项，行为权威以 JDK 源码 + test/jdk 为准。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`ExtendedOpenOptionTest.testBlackBox()`）覆盖：
- `values()` 返回声明序 `[NOSHARE_READ, NOSHARE_WRITE, NOSHARE_DELETE, DIRECT]`，且每次返回新数组。
- 每个常量的 `ordinal()` 与下标一致、`toString()==name()`、`getDeclaringClass()`、`instanceof OpenOption`、`valueOf(name)` 恒等。
- `compareTo` 按序；`valueOf("DIRECT2")` 抛 `IllegalArgumentException`；`valueOf(null)` 抛 `NullPointerException`。
白盒部分补齐：isEnum/final/extends Enum/implements OpenOption/四字段存在/四常量名称。

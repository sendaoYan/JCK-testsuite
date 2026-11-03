# com.sun.nio.file.ExtendedCopyOption 测试设计

## 1. API 功能
- 类型：`public enum ExtendedCopyOption implements java.nio.file.CopyOption`（模块 `jdk.unsupported`）。
- 枚举常量：`INTERRUPTIBLE`（允许复制过程被 `Thread.interrupt` 中断）。
- 方法：`values()`（按声明序返回常量数组）、`valueOf(String)`（按名称取常量，名称非法抛 `IllegalArgumentException`、null 抛 `NullPointerException`）、继承自 `Enum` 的 `name()/ordinal()/toString()/compareTo()/equals()/hashCode()/getDeclaringClass()`。
- 无其它业务方法；`CopyOption` 为标记接口。

## 2. Java 用户如何使用
```java
import java.nio.file.*;
import com.sun.nio.file.ExtendedCopyOption;
Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING, ExtendedCopyOption.INTERRUPTIBLE);
// 枚举取值/遍历
ExtendedCopyOption o = ExtendedCopyOption.valueOf("INTERRUPTIBLE");
for (ExtendedCopyOption v : ExtendedCopyOption.values()) { ... }
```

## 3. 开源代码实际用法调研
- JDK 源码定义：`~/git/jdk25u-dev/src/jdk.unsupported/share/classes/com/sun/nio/file/ExtendedCopyOption.java`，常量 `INTERRUPTIBLE(FileSystemOption.INTERRUPTIBLE)`。
- JDK 自带测试：`~/git/jdk25u-dev/test/jdk/java/nio/file/Files/InterruptCopy.java`（`@modules jdk.unsupported`），把 `ExtendedCopyOption.INTERRUPTIBLE` 作为 `Files.copy` 的选项，验证复制可被中断。
- `SBC.java`（`test/jdk/java/nio/file/Files/SBC.java`）同样使用 `ExtendedCopyOption.INTERRUPTIBLE`。
- 第三方框架（Spring/Guava/Netty）极少使用该 JDK 内部/专有 API，故行为权威以 JDK 源码 + test/jdk 为准。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`ExtendedCopyOptionTest.testBlackBox()`）覆盖：
- 常量属性：`name()/toString()/ordinal()==0/getDeclaringClass()`、`compareTo` 自比较、`equals/hashCode` 稳定。
- `values()` 每次返回新数组但内容一致、内容稳定。
- `valueOf("INTERRUPTIBLE")` 恒等；`valueOf("NOPE")` 抛 `IllegalArgumentException`；`valueOf(null)` 抛 `NullPointerException`。
白盒部分（`testWhiteBox()`）补齐：isEnum/final/extends Enum/implements CopyOption/字段存在/单常量/valueOf 恒等。

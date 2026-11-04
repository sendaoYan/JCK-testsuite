# com.sun.nio.file.SensitivityWatchEventModifier 测试设计

## 1. API 功能
- 类型：`public enum SensitivityWatchEventModifier implements java.nio.file.WatchEvent.Modifier`（模块 `jdk.unsupported`，`@Deprecated(since="21", forRemoval=true)`）。
- 枚举常量（声明序）：`HIGH`（2 秒）、`MEDIUM`（10 秒）、`LOW`（30 秒）。
- 查询方法：`int sensitivityValueInSeconds()` 返回对应秒数（注意方法名带 `InSeconds` 后缀，不是 `sensitivityValue()`）。
- 方法：`values()`、`valueOf(String)` 及 `Enum` 继承方法。

## 2. Java 用户如何使用
```java
import java.nio.file.*;
import com.sun.nio.file.SensitivityWatchEventModifier;
WatchService ws = FileSystems.getDefault().newWatchService();
Path dir = Path.of("dir");
dir.register(ws, new WatchEvent.Kind<?>[]{ StandardWatchEventKinds.ENTRY_MODIFY },
             SensitivityWatchEventModifier.HIGH);
int secs = SensitivityWatchEventModifier.HIGH.sensitivityValueInSeconds(); // 2
```

## 3. 开源代码实际用法调研
- JDK 源码定义：`~/git/jdk25u-dev/src/jdk.unsupported/share/classes/com/sun/nio/file/SensitivityWatchEventModifier.java`：`HIGH(FileSystemOption.SENSITIVITY_HIGH, 2)`、`MEDIUM(..., 10)`、`LOW(..., 30)`，`sensitivityValueInSeconds()` 返回 `sensitivity`。
- JDK 自带测试：`~/git/jdk25u-dev/test/jdk/java/nio/file/WatchService/SensitivityModifier.java`（`@modules jdk.unsupported`，随机取一个 sensitivity 常量注册并校验注册有效）。
- 第三方框架基本不直接用，行为权威以 JDK 源码 + test/jdk 为准。

## 4. 测试用例设计 + 自动化实现
黑盒部分（`SensitivityWatchEventModifierTest.testBlackBox()`）覆盖：
- `values()` 返回 `[HIGH, MEDIUM, LOW]` 声明序。
- `ordinal()` 为 0/1/2；敏感值满足 `HIGH(2) < MEDIUM(10) < LOW(30)` 的关系。
- 每个常量 `toString()==name()`、`getDeclaringClass()`、`instanceof Modifier`、`valueOf(name)` 恒等。
- `compareTo` 顺序；`valueOf("HIGHEST")` 抛 `IllegalArgumentException`；`valueOf(null)` 抛 `NullPointerException`。
白盒部分补齐：isEnum/final/extends Enum/implements Modifier/存在 `sensitivityValueInSeconds` 方法/三字段/三个确定秒数。

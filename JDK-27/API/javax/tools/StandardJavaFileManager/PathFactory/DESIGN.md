# javax.tools.StandardJavaFileManager.PathFactory 测试设计

## 1. API 功能
`PathFactory` 是函数式接口，方法 `Path getPath(String first, String... more)` 把路径字符串序列拼成一个 `Path`。由 `StandardJavaFileManager.setPathFactory(PathFactory)` 安装，默认等价于 `Paths.get(first, more)`。

## 2. Java 用户如何使用
```java
fm.setPathFactory((first, more) -> Path.of(first, more));
```
用于在自定义文件系统（如 Jimfs、内存 FS）下把位置字符串映射成 `Path`。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/StandardJavaFileManager.java`：`PathFactory` 接口与 `setPathFactory` 默认实现。
- `~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/tools/javac/file/JavacFileManager.java`：`setPathFactory` 用 `Objects.requireNonNull(f)` 并存入 `pathFactory`（私有 `getPath` 使用）。

## 4. 测试用例设计 + 自动化实现
- **白盒**：接口、无父接口、声明 `getPath`。
- **黑盒**：自定义 `PathFactory` 的 `getPath` 单段/多段拼接等于 `Path.of(...)`；把工厂装到真实 `StandardJavaFileManager`（不抛），并断言 `setPathFactory(null)` 抛 `NullPointerException`。

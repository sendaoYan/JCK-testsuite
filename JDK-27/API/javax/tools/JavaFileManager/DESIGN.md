# javax.tools.JavaFileManager 测试设计

## 1. API 功能
`JavaFileManager` 是编译器的文件系统抽象，继承 `Closeable`、`Flushable`、`OptionChecker`。主要方法：
- 类加载/查询：`getClassLoader(Location)`、`list(Location, packageName, Set<Kind>, recurse)`、`inferBinaryName(Location, JavaFileObject)`、`isSameFile(FileObject, FileObject)`。
- 选项：`isSupportedOption`、`handleOption`、`hasLocation`。
- 输入输出：`getJavaFileForInput`、`getJavaFileForOutput`、`getFileForInput`、`getFileForOutput`。
- 生命周期：`flush`、`close`。
（`setLocation`/`getLocation` 在 `StandardJavaFileManager` 子接口上。）

## 2. Java 用户如何使用
```java
JavaFileManager fm = compiler.getStandardFileManager(null, null, null);
fm.handleOption("-classpath", List.of("cp").iterator());
fm.inferBinaryName(StandardLocation.SOURCE_PATH, jfo);
fm.isSameFile(a, b);
fm.close();
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/tools/javac/file/JavacFileManager.java`：`JavaFileManager` 具体实现。
- `~/git/jdk25u-dev/test/langtools/tools/lib/snippets/SnippetUtils.java`：`JavaFileManager fm = compiler.getStandardFileManager(dl, null, null);`。
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/ForwardingJavaFileManager.java`：把接口方法逐一委托。

## 4. 测试用例设计 + 自动化实现
- **白盒**：接口、继承 `Closeable`/`Flushable`/`OptionChecker`、声明关键方法。
- **黑盒**：借 `StandardJavaFileManager` 实例：`setLocation(SOURCE_PATH)` + 写源文件 + `getJavaFileObjects`，断言 `hasLocation`、`inferBinaryName=="X"`、`isSameFile(self)` true、`handleOption("-classpath")` true、`isSupportedOption("-classpath")==1`、`getClassLoader` 非空、`list` 非空、`getJavaFileForOutput` 非空、`flush`。finally close + 清理。

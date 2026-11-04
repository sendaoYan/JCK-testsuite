# javax.tools.StandardJavaFileManager 测试设计

## 1. API 功能
`StandardJavaFileManager` 继承 `JavaFileManager`，是基于真实文件系统的文件管理器。方法：
- 位置：`setLocation(Location, Iterable<File>)`、`getLocation(Location)`、`getLocationAsPaths(Location)`、`setLocationFromPaths`。
- 对象工厂：`getJavaFileObjects(File...)`、`getJavaFileObjects(Path...)`、`getJavaFileObjects(String...)`、`getJavaFileObjectsFromFiles`、`getJavaFileObjectsFromPaths`、`getJavaFileObjectsFromStrings`。
- 其他：`isSameFile`、`asPath`、`setPathFactory`。

## 2. Java 用户如何使用
```java
StandardJavaFileManager fm = compiler.getStandardFileManager(null, Locale.ROOT, UTF_8);
fm.setLocation(StandardLocation.CLASS_OUTPUT, List.of(dir.toFile()));
Iterable<? extends JavaFileObject> units = fm.getJavaFileObjects(file1, file2);
fm.isSameFile(a, b); fm.close();
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/langtools/tools/javac/T6956638.java`：`compiler.getStandardFileManager(diagnosticCollector, null, null)` 与 `fileManager.getJavaFileObjects(sourceFiles)`。
- `~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/tools/javac/file/JavacFileManager.java`：`StandardJavaFileManager` 实现。
- `~/git/jdk25u-dev/test/langtools/tools/lib/snippets/SnippetUtils.java`：`compiler.getStandardFileManager(dl, null, null)`。

## 4. 测试用例设计 + 自动化实现
- **白盒**：接口、继承 `JavaFileManager`、声明 `isSameFile`/`getJavaFileObjects*`/`setLocation`/`getLocation`/`asPath`。
- **黑盒**：临时目录 `setLocation(CLASS_OUTPUT)` + `getLocation`/`getLocationAsPaths` 往返；写 `X.java`/`Z.java`，用 `getJavaFileObjects(File/Path)` 与 `getJavaFileObjectsFromStrings/FromFiles` 打开，断言 kind=SOURCE、uri 一致、`isSameFile` true；`getFileForOutput` 非空且 name 正确。finally close + 清理。

# javax.tools.JavaFileObject 测试设计

## 1. API 功能
`JavaFileObject` 继承 `FileObject`，是 Java 源码/类文件/HTML 等工具文件的抽象。方法：
- `getKind()` 返回 `JavaFileObject.Kind`。
- `isNameCompatible(String simpleName, Kind kind)` 判断文件名是否与简单类名 + 扩展名兼容。
- `getNestingKind()` / `getAccessLevel()` 返回嵌套级别与访问级别提示（未知返回 null）。

## 2. Java 用户如何使用
```java
JavaFileObject src = SimpleJavaFileObject.forSource(URI.create("string:///X.java"), "class X {}");
src.getKind(); src.isNameCompatible("X", Kind.SOURCE); src.getCharContent(false);
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/SimpleJavaFileObject.java`：`isNameCompatible` 比较 URI path 与 `simpleName + kind.extension`；`getNestingKind`/`getAccessLevel` 返回 null。
- `~/git/jdk25u-dev/test/langtools/tools/lib/snippets/SnippetUtils.java`：`SimpleJavaFileObject.forSource(uri, compUnit)` 构造源对象。
- `~/git/jdk25u-dev/test/langtools/tools/javac/T6956638.java`：`fileManager.getJavaFileObjects(sourceFiles)` 返回 `JavaFileObject` 集合。

## 4. 测试用例设计 + 自动化实现
- **白盒**：接口、继承 `FileObject`、声明 `getKind`/`isNameCompatible`/`getNestingKind`/`getAccessLevel`。
- **黑盒**：用 `SimpleJavaFileObject` 断言 `getKind`、`isNameCompatible` 正/反例、`getNestingKind`/`getAccessLevel` 为 null、`getCharContent`/`toUri` 往返；`forSource` 工厂的 kind 与内容。

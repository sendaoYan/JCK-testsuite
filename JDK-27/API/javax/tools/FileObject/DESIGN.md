# javax.tools.FileObject 测试设计

## 1. API 功能
`FileObject` 是工具可访问的文件抽象，是 `JavaFileObject` 的父接口。方法：
- 标识：`toUri()`、`getName()`。
- 读写：`openInputStream()`、`openOutputStream()`、`openReader(boolean)`、`openWriter()`、`getCharContent(boolean)`。
- 元信息/清理：`getLastModified()`、`delete()`。

## 2. Java 用户如何使用
通过 `JavaFileManager.getFileForOutput(...)` 拿到 `FileObject`，再 `openWriter()` 写、`getCharContent(false)`/`openReader(false)` 读、`delete()` 删除：
```java
FileObject fo = fm.getFileForOutput(StandardLocation.CLASS_OUTPUT, "", "data.txt", null);
fo.openWriter().write("x"); ... fo.getCharContent(false); fo.delete();
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/SimpleJavaFileObject.java`：`SimpleJavaFileObject` 是 `FileObject`/`JavaFileObject` 的基础实现，`openReader` 包装 `getCharContent`，`openWriter` 包装 `openOutputStream`。
- `~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/tools/javac/file/JavacFileManager.java`：`getFileForOutput` 返回 `PathFileObject`（实现 `FileObject`）。
- javac 注解处理器 `Filer` 通过文件管理器读写生成文件。

## 4. 测试用例设计 + 自动化实现
- **白盒**：接口、无父接口、声明全部 9 个方法。
- **黑盒**：`getStandardFileManager` → `setLocation(CLASS_OUTPUT)` → `getFileForOutput` 拿 `FileObject`，断言 `toUri`/`getName`/`getLastModified>=0`；`openWriter` 写 + `getCharContent`/`openReader` 读回一致；`isSameFile(self)` true；`delete()` true。finally 清理临时目录。

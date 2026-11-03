# javax.tools.SimpleJavaFileObject 测试设计

## 1. API 功能
`SimpleJavaFileObject` 是 `JavaFileObject` 的基础实现，供子类化使用。持有 `protected final URI uri` 与 `protected final Kind kind`；protected 构造器要求 URI 有非空 path。方法：
- 基础：`toUri()`（返回 uri）、`getName()`（返回 uri 的 path）、`toString()`。
- 实现：`getKind()` 返回 kind；`getLastModified()` 返回 0；`delete()` 返回 false；`getNestingKind()`/`getAccessLevel()` 返回 null。
- 可选：`openInputStream`/`openOutputStream`/`getCharContent` 默认抛 `UnsupportedOperationException`；`openReader` 包装 `getCharContent`，`openWriter` 包装 `openOutputStream`。
- 工厂：`forSource(URI, String)` 返回内容固定的 SOURCE 对象。

## 2. Java 用户如何使用
```java
JavaFileObject src = new SimpleJavaFileObject(URI.create("string:///X.java"), Kind.SOURCE) {
    @Override public CharSequence getCharContent(boolean ignore) { return "class X {}"; }
};
// 或
JavaFileObject src = SimpleJavaFileObject.forSource(URI.create("string:///X.java"), "class X {}");
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/SimpleJavaFileObject.java`：实现与 `forSource` 的 javadoc 内嵌内存编译示例（`compiler.getTask(... List.of(SimpleJavaFileObject.forSource(...)))`）。
- `~/git/jdk25u-dev/test/langtools/tools/lib/snippets/SnippetUtils.java`：`SimpleJavaFileObject.forSource(uri, compUnit)`。
- `~/git/jdk25u-dev/test/langtools/tools/javac/parser/ReversedSourcePositions.java`：`new SimpleJavaFileObject(...)` 子类。

## 4. 测试用例设计 + 自动化实现
- **白盒**：非接口、实现 `JavaFileObject`、`uri`/`kind` 字段 protected final、构造器 protected。
- **黑盒**：子类断言 `toUri`/`getName`/`getKind`/`isNameCompatible` 正反例/`getNestingKind` null/`getAccessLevel` null/`getLastModified` 0/`delete` false/`getCharContent`；`openReader` 读回；`openInputStream`/`openOutputStream` 抛 UnsupportedOperationException；`toString` 含 uri；`forSource` kind/content/name；构造器拒绝无 path URI（IllegalArgumentException）。

# javax.tools.JavaCompiler 测试设计

## 1. API 功能
`JavaCompiler` 是 javac 的编程接口，继承 `Tool` 和 `OptionChecker`。核心方法：
- `getTask(Writer out, JavaFileManager fm, DiagnosticListener, Iterable<String> options, Iterable<String> classes, Iterable<? extends JavaFileObject> sources)` 创建 `CompilationTask`。
- `getStandardFileManager(DiagnosticListener, Locale, Charset)` 创建标准文件管理器。
继承：`name()`（`"javac"`）、`run(...)`、`getSourceVersions()`、`isSupportedOption(String)`。

## 2. Java 用户如何使用
```java
JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
if (jc == null) { /* 非完整 JDK */ }
DiagnosticCollector<JavaFileObject> dc = new DiagnosticCollector<>();
JavaFileObject src = SimpleJavaFileObject.forSource(URI.create("string:///X.java"), "class X {}");
boolean ok = jc.getTask(null, null, dc, List.of("-proc:none"), null, List.of(src)).call();
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/langtools/tools/javac/T6956638.java`：`ToolProvider.getSystemJavaCompiler()` + `getStandardFileManager` + `getJavaFileObjects` + `getTask`。
- `~/git/jdk25u-dev/test/langtools/tools/lib/snippets/SnippetUtils.java`：`compiler.getTask(pw, fm, dl, opts, null, List.of(fo))`。
- `~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/tools/javac/api/JavacTool.java`：`name()`、`isSupportedOption`（遍历 `Option.getJavacToolOptions()`，有独立参数返回 1、无返回 0、不认识返回 -1）。

## 4. 测试用例设计 + 自动化实现
- **白盒**：接口、继承 `Tool`+`OptionChecker`、声明 `getTask`/`getStandardFileManager`。
- **黑盒**：`getSystemJavaCompiler()` 为 null 时降级；否则断言 `name()=="javac"`、`getSourceVersions` 含 `RELEASE_27`、`isSupportedOption("-source")==1`/`("-g")==0`/`("--bogus")==-1`、`getStandardFileManager` 非空；内存编译合法源成功且无诊断、非法源失败且有诊断。

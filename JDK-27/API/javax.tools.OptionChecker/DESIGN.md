# javax.tools.OptionChecker 测试设计

## 1. API 功能
`OptionChecker` 是选项检查接口，方法 `int isSupportedOption(String option)` 返回该选项消耗的参数个数：正数表示支持并消耗 N 个参数、0 表示支持且无参数、-1 表示不支持。`JavaCompiler`、`DocumentationTool`、`JavaFileManager` 都继承它。

## 2. Java 用户如何使用
```java
OptionChecker oc = ToolProvider.getSystemJavaCompiler();
if (oc.isSupportedOption("-source") >= 0) { /* 支持 */ }
```
工具（javac）与文件管理器识别的选项集合不同：`-source`/`-g` 属 javac，`-classpath`/`-cp` 属文件管理器。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/tools/javac/api/JavacTool.java`：`isSupportedOption` 遍历 `Option.getJavacToolOptions()`，`o.matches(option)` 命中时返回 `o.hasSeparateArg() ? 1 : 0`，否则 -1。
- `~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/tools/javac/file/JavacFileManager.java`：识别 `-classpath`/`-d`/`-sourcepath` 等路径选项。
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/JavaFileManager.java`：声明继承 `OptionChecker`。

## 4. 测试用例设计 + 自动化实现
- **白盒**：接口、无父接口、声明 `isSupportedOption`。
- **黑盒**：javac（`OptionChecker`）断言 `-source`==1、`-target`==1、`-g`==0、`-Xlint`==0、`--bogus`==-1；`StandardJavaFileManager`（`OptionChecker`）断言 `-classpath`/`-cp`/`-sourcepath`==1、`--bogus`==-1。

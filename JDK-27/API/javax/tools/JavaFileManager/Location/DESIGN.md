# javax.tools.JavaFileManager.Location 测试设计

## 1. API 功能
`JavaFileManager.Location` 是文件管理器位置的接口：`getName()` 返回名称、`isOutputLocation()` 表示是否输出位置、`isModuleOrientedLocation()`（默认 false）表示是否面向模块。标准实现是 `StandardLocation`；javadoc 有 `DocumentationTool.Location`。

## 2. Java 用户如何使用
```java
JavaFileManager.Location loc = StandardLocation.CLASS_OUTPUT;
loc.getName(); loc.isOutputLocation(); loc.isModuleOrientedLocation();
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/StandardLocation.java`：实现该接口并定义 13 个位置。
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/DocumentationTool.java`：`Location` 枚举实现该接口。
- javac 内部 `com.sun.tools.javac.file.Locations` 基于 `Location` 做路径解析。

## 4. 测试用例设计 + 自动化实现
- **白盒**：接口、无父接口、声明 `getName`/`isOutputLocation`/`isModuleOrientedLocation`。
- **黑盒**：用 `StandardLocation` 与 `DocumentationTool.Location` 实例断言 `getName`、`isOutputLocation`、`isModuleOrientedLocation`（如 `MODULE_PATH` 面向模块、`SOURCE_PATH` 非输出）。

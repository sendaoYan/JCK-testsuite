# javax.tools.StandardLocation 测试设计

## 1. API 功能
`StandardLocation` 是实现 `JavaFileManager.Location` 的标准位置枚举，共 13 个：`CLASS_OUTPUT`、`SOURCE_OUTPUT`、`CLASS_PATH`、`SOURCE_PATH`、`ANNOTATION_PROCESSOR_PATH`、`ANNOTATION_PROCESSOR_MODULE_PATH`、`PLATFORM_CLASS_PATH`、`NATIVE_HEADER_OUTPUT`、`MODULE_SOURCE_PATH`、`UPGRADE_MODULE_PATH`、`SYSTEM_MODULES`、`MODULE_PATH`、`PATCH_MODULE_PATH`。方法：`getName()`（返回枚举名）、`isOutputLocation()`、`isModuleOrientedLocation()`、`locationFor(String)`。

## 2. Java 用户如何使用
```java
fm.setLocation(StandardLocation.CLASS_OUTPUT, dirs);
StandardLocation.CLASS_OUTPUT.isOutputLocation();
StandardLocation.locationFor("MODULE_PATH");
```

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.compiler/share/classes/javax/tools/StandardLocation.java`：常量、`getName`、`isOutputLocation`（CLASS/SOURCE/NATIVE_HEADER_OUTPUT）、`isModuleOrientedLocation`（6 个模块位置）、`locationFor`。
- `~/git/jdk25u-dev/src/jdk.compiler/share/classes/com/sun/tools/javac/file/JavacFileManager.java`：大量使用 `StandardLocation`。

## 4. 测试用例设计 + 自动化实现
- **白盒**：枚举、继承 `Enum`、13 常量、实现 `Location`、声明 `locationFor`/`getName`/`isOutputLocation`/`isModuleOrientedLocation`。
- **黑盒**：`values()` 精确顺序与 name；输出位置（CLASS/SOURCE/NATIVE_HEADER_OUTPUT）；模块位置（MODULE_SOURCE_PATH/ANNOTATION_PROCESSOR_MODULE_PATH/UPGRADE_MODULE_PATH/SYSTEM_MODULES/MODULE_PATH/PATCH_MODULE_PATH）；`locationFor` 已知名往返 + 自定义名（`CUSTOM_OUTPUT` 推断为输出）。

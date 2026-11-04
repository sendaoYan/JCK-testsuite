# java.util.prefs.Preferences 测试方案

白盒结构断言在 `PreferencesTest.testWhiteBox()`，黑盒行为断言在 `PreferencesTest.testBlackBox()`。为避免污染真实用户/系统首选项，通过 `-Djava.util.prefs.PreferencesFactory=PreferencesTest$MemFactory` 注入 in-memory 工厂（测试 finally 恢复原属性）。

## 1. API 功能（按方法分组）

- **常量**：`MAX_KEY_LENGTH = 80`、`MAX_VALUE_LENGTH = 8192`、`MAX_NAME_LENGTH = 80`。
- **静态工厂/入口**：`userRoot()`、`systemRoot()`、`userNodeForPackage(Class)`、`systemNodeForPackage(Class)`、`importPreferences(InputStream)`。
- **抽象实例方法**：`put/get/remove/clear`、类型化 `putInt/getInt` 等、`keys/childrenNames`、`parent/node/nodeExists/removeNode/name/absolutePath/isUserNode/toString/flush/sync`、监听注册、`exportNode/exportSubtree`。

## 2. Java 用户如何使用

- 入口：`Preferences.userRoot()` / `Preferences.systemRoot()` / `Preferences.userNodeForPackage(MyClass.class)`。
- 组合链路：`userRoot().node("app/prefs").put("k","v")`；`get("k","default")`；`node("child").parent()`；`nodeExists`；`removeNode()`。
- `userNodeForPackage` 将包名 `.` 换成 `/` 得到节点路径（如 `java.util.Map` → `/java/util`）。

## 3. 开源代码实际用法调研

> 说明：本环境 WebSearch 不可用，调研基于 JDK-27 实现源码 `~/git/jdk25u-dev` 与自带测试 `test/jdk`。

- `src/java.desktop/share/classes/sun/awt/im/InputContext.java:1039-1044`：先 `userRoot()` 查用户树，失败再 `systemRoot()` 查系统树。
- `src/jdk.jshell/share/classes/jdk/internal/jshell/tool/JShellToolBuilder.java:237`：`Preferences.userRoot().node(PREFERENCES_NODE)` 建命名节点存储持久化配置。
- `src/utils/IdealGraphVisualizer/Settings/src/main/java/com/sun/hotspot/igv/settings/Settings.java:57`：`Preferences.userNodeForPackage(Settings.class)` 按包取节点。
- `test/jdk/java/util/prefs/ExportNode.java:45`：`Preferences.userRoot().node("ExportNodeTest1")` 建链 + `put` + `exportNode`。
- `test/jdk/java/util/prefs/PrefsSpiTest.java:85`：`-Djava.util.prefs.PreferencesFactory=StubPreferencesFactory` 注入自定义工厂。

## 4. 测试用例设计 + 自动化实现

- **白盒**：public abstract、`extends Object`、`protected` 无参构造器、三个 `public static final int` 常量值精确、静态入口与抽象实例方法存在。
- **黑盒**（自定义工厂）：`userRoot().isUserNode()==true`、`systemRoot().isUserNode()==false`、二者不同且各自缓存；常量精确；`userNodeForPackage/java.util.Map.class` 返回 `/java/util` 且 user/system 属性正确；in-memory 节点上 `put/get/putInt/getInt/node/parent/name/absolutePath/nodeExists/keys/childrenNames/flush/sync/toString/removeNode` 多步组合；`toString()` 精确匹配 `User Preference Node: /jck_prefs_test`。
- **健壮性**：工厂属性在 `finally` 恢复原值；in-memory TreeMap 不落盘、不写真实 prefs；固定输入、无墙钟/无 Random、无 HashMap 迭代顺序断言。

自动化实现：`testWhiteBox()` + `testBlackBox()`，共 34 断言。

# java.awt.Taskbar.Feature 黑盒行为测试设计

## 1. API 功能
- `enum Taskbar.Feature`：`ICON_BADGE_TEXT`、`ICON_BADGE_NUMBER`、`ICON_BADGE_IMAGE_WINDOW`、`ICON_IMAGE`、`MENU`、`PROGRESS_STATE_WINDOW`、`PROGRESS_VALUE`、`PROGRESS_VALUE_WINDOW`、`USER_ATTENTION`、`USER_ATTENTION_WINDOW`。
- 枚举 API：`values()`、`valueOf(String)`、`ordinal()`、`name()`。

## 2. Java 用户如何使用
`Taskbar.isSupported(Feature)` 探测任务栏能力：
```java
boolean badge = taskbar.isSupported(Taskbar.Feature.ICON_BADGE_TEXT);
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/Taskbar.java`：`Feature` 内部枚举。
- `test/jdk/java/awt/Taskbar/` 遍历 Feature 做能力探测。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- `values().length==10`；前五 ordinal 顺序；关键常量非空。
- `valueOf(name)`/`name()` 往返；10 常量两两互异。
- `valueOf("BOGUS")` 抛 `IllegalArgumentException`。
headless-safe，按约定放 headful 轮。

# java.awt.Taskbar.State 黑盒行为测试设计

## 1. API 功能
- `enum Taskbar.State`：`OFF`、`NORMAL`、`PAUSED`、`INDETERMINATE`、`ERROR`（进度状态）。
- 枚举 API：`values()`、`valueOf(String)`、`ordinal()`、`name()`。

## 2. Java 用户如何使用
`Taskbar.setWindowProgressState(window, State.ERROR)` 设置任务栏进度状态：
```java
taskbar.setWindowProgressState(win, Taskbar.State.INDETERMINATE);
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/Taskbar.java`：`State` 内部枚举。
- `test/jdk/java/awt/Taskbar/` 用 State 设置窗口进度状态。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- `values().length==5`；ordinal 顺序 0..4。
- `valueOf(name)`/`name()` 往返；五常量互异。
- `valueOf("BOGUS")` 抛 `IllegalArgumentException`。
headless-safe，按约定放 headful 轮。

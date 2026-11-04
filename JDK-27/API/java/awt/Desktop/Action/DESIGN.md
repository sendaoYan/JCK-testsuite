# java.awt.Desktop.Action 黑盒行为测试设计

## 1. API 功能
- `enum Desktop.Action`：`OPEN`、`EDIT`、`PRINT`、`MAIL`、`BROWSE`、`APP_EVENT_*`、`APP_*`、`BROWSE_FILE_DIR`、`MOVE_TO_TRASH` 共 24 个动作。
- 枚举 API：`values()`、`valueOf(String)`、`ordinal()`、`name()`。

## 2. Java 用户如何使用
`Desktop.isSupported(Action)` 探测某项桌面集成是否可用：
```java
boolean canOpen = Desktop.getDesktop().isSupported(Desktop.Action.OPEN);
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/Desktop.java`：`Action` 内部枚举，声明顺序即 ordinal。
- `test/jdk/java/awt/Desktop/` 遍历 Action 调用 isSupported。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- `values().length==24`；前五 ordinal 顺序；关键常量非空。
- `valueOf(name)`/`name()` 往返；24 常量两两互异。
- `valueOf("BOGUS")` 抛 `IllegalArgumentException`。
headless-safe（枚举本身），但按本批约定随 Desktop 放 headful 轮。

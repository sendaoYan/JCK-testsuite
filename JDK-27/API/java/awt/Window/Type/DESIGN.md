# java.awt.Window.Type 黑盒行为测试设计

## ① API 功能分组
- 枚举常量：`NORMAL`、`UTILITY`、`POPUP`。
- 继承方法：`values()`/`valueOf(String)`/`ordinal()`/`name()`/`toString`。

## ② Java 用户用法
```java
Window w = new Window(frame);
w.setType(Window.Type.UTILITY);
Window.Type t = w.getType();
Window.Type[] all = Window.Type.values();
```
组合链路：Window 设置/读取类型 → values 遍历校验。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Window.setType`/`getType` 存取 `type` 字段（`Window.java`）。
- `Window.setOpacity` 前校验 `type`，`UTILITY`/`POPUP` 才允许透明（`Window.java`）。
典型外部用法：GUI 应用把工具窗口设为 `UTILITY`，弹出式窗口设为 `POPUP`。

## ④ 测试用例设计 + 自动化实现
- `values()` 长度与顺序 → `testBlackBox`。
- `ordinal()` 固定序数 → `testBlackBox`。
- `valueOf(name)` 往返 + 字面量 + `toString` → `testBlackBox`。

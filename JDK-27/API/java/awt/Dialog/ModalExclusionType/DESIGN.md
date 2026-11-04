# java.awt.Dialog.ModalExclusionType 黑盒行为测试设计

## ① API 功能分组
- 枚举常量：`NO_EXCLUDE`、`APPLICATION_EXCLUDE`、`TOOLKIT_EXCLUDE`。
- 继承方法：`values()`/`valueOf(String)`/`ordinal()`/`name()`。

## ② Java 用户用法
```java
window.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
Dialog.ModalExclusionType t = window.getModalExclusionType();
Dialog.ModalExclusionType[] all = Dialog.ModalExclusionType.values();
```
组合链路：Window 设置/读取排除类型 → values 遍历校验。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Window.setModalExclusionType`/`getModalExclusionType` 存取 `modalExclusionType` 字段（`Window.java`）。
- `Window.isModalExcluded(Dialog.ModalExclusionType)` 按类型层级比较（`Window.java`）。
典型外部用法：GUI 应用中把托盘/工具窗口设为 `APPLICATION_EXCLUDE` 使其不被模态对话框阻塞。

## ④ 测试用例设计 + 自动化实现
- `values()` 长度与顺序 → `testBlackBox`。
- `ordinal()` 固定序数 → `testBlackBox`。
- `valueOf(name)` 往返 + 字面量 → `testBlackBox`。

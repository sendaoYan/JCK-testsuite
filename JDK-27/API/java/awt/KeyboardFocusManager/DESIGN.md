# java.awt.KeyboardFocusManager 黑盒行为测试设计

## ① API 功能分组
- 常量：`FORWARD/BACKWARD/UP_CYCLE/DOWN_CYCLE_TRAVERSAL_KEYS`。
- 单例：`getCurrentKeyboardFocusManager/setCurrentKeyboardFocusManager`。
- 遍历策略：`getDefaultFocusTraversalPolicy/setDefaultFocusTraversalPolicy`。
- 遍历键：`getDefaultFocusTraversalKeys/setDefaultFocusTraversalKeys`。
- 焦点查询：`getFocusOwner/getFocusedWindow/getActiveWindow`。
- 事件分发：`addKeyEventDispatcher/removeKeyEventDispatcher`、`focusNextComponent/focusPreviousComponent`。

## ② Java 用户用法
```java
KeyboardFocusManager km = KeyboardFocusManager.getCurrentKeyboardFocusManager();
km.getDefaultFocusTraversalPolicy();
km.getFocusOwner();
```
组合链路：取单例 → 查默认遍历策略/遍历键集合 → 查询焦点组件（不依赖焦点事件）。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `getCurrentKeyboardFocusManager` 返回进程内唯一实例（`KeyboardFocusManager.java`）。
- `getDefaultFocusTraversalKeys` 返回默认遍历键集合（`KeyboardFocusManager.java`）。
- 常量值定义见 `KeyboardFocusManager.java`（0/1/2/3）。

## ④ 测试用例设计 + 自动化实现
- 常量精确值 → `testBlackBox`。
- 单例非空且为 KeyboardFocusManager 类型 → `testBlackBox`。
- getDefaultFocusTraversalPolicy 非空 + 遍历键集合非空 → `testBlackBox`。

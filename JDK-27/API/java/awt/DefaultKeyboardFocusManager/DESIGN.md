# java.awt.DefaultKeyboardFocusManager 黑盒行为测试设计

## ① API 功能分组
- 构造：`DefaultKeyboardFocusManager()`。
- 继承 KeyboardFocusManager：`getDefaultFocusTraversalPolicy/getDefaultFocusTraversalKeys`。
- 分发/处理：`dispatchEvent/dispatchKeyEvent/postProcessKeyEvent/processKeyEvent`。
- 焦点遍历：`focusNextComponent/focusPreviousComponent/upFocusCycle/downFocusCycle`。

## ② Java 用户用法
```java
DefaultKeyboardFocusManager d = new DefaultKeyboardFocusManager();
d.getDefaultFocusTraversalPolicy();
d.focusNextComponent(someComponent);
```
组合链路：构造实例 → 查默认遍历策略/遍历键 → （可选）焦点遍历方法。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `DefaultKeyboardFocusManager` 是 KeyboardFocusManager 的默认实现（`DefaultKeyboardFocusManager.java`）。
- 通常由 `KeyboardFocusManager.getCurrentKeyboardFocusManager()` 返回该实现。
- `focusNextComponent/focusPreviousComponent` 实现焦点遍历逻辑。

## ④ 测试用例设计 + 自动化实现
- 实例是 KeyboardFocusManager 子类 → `testBlackBox`。
- getDefaultFocusTraversalPolicy 非空 → `testBlackBox`。
- getDefaultFocusTraversalKeys(FORWARD) 非空 → `testBlackBox`。

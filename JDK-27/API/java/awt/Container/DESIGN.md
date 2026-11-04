# java.awt.Container 黑盒行为测试设计

## ① API 功能分组
- 子组件管理：`add`（三形态）、`remove(int/Component)`、`removeAll`、`getComponent`、`getComponentCount`、`getComponents`。
- 布局：`setLayout/getLayout/doLayout/validate/invalidate/getInsets`。
- 命中/祖先：`getComponentAt/findComponentAt/isAncestorOf`。
- 焦点遍历：`setFocusTraversalPolicy/getFocusTraversalPolicy`。

## ② Java 用户用法
```java
Container c = new Container();
c.setLayout(new FlowLayout());
c.add(new Button("a")); c.add(new Button("b"));
c.validate();
c.getComponentCount(); c.getComponent(0); c.remove(0);
```
组合链路：设布局 → add 组件 → doLayout → 读回子组件 bounds/顺序 → remove/removeAll。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Container.addImpl` 维护 `component` 列表并触发 addNotify（`Container.java`）。
- `Container.layout` 调用 `layoutMgr.layoutContainer(this)`（`Container.java`）。
- `Container.validate` 仅当 `!isValid() && peer != null` 时 validateTree（`Container.java`）。

## ④ 测试用例设计 + 自动化实现
- 三形态 `add` + `getComponentCount/getComponent/getComponents` 顺序 → `testBlackBox`。
- `remove(Component)/remove(int)/removeAll` → `testBlackBox`。
- FlowLayout 布局两组件精确 bounds → `testBlackBox`。
- `invalidate/validate/isValid` 状态翻转 → `testBlackBox`。
- `getInsets/findComponentAt/getComponentAt` 非空 → `testBlackBox`。
- `setFocusTraversalPolicy/getFocusTraversalPolicy` 往返 → `testBlackBox`。

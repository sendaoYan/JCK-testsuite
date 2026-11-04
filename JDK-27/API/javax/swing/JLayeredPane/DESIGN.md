# javax.swing.JLayeredPane 测试设计

## 1. API 功能
- 层级常量：`DEFAULT_LAYER(0)/PALETTE_LAYER(100)/MODAL_LAYER(200)/POPUP_LAYER(300)/DRAG_LAYER(400)/FRAME_CONTENT_LAYER(-30000)`、`LAYER_PROPERTY`。
- 层级管理：`add(Component, Object)`、`setLayer(Component,int[,int])`、`getLayer(Component)`、`getIndexOf(Component)`、`getPosition(Component)`、`setPosition(Component,int)`、`moveToFront/moveToBack`。
- 静态工具：`putLayer(JComponent,int)`、`getLayer(JComponent)`、`getLayeredPaneAbove(Component)`。
- 查询：`highestLayer/lowestLayer/getComponentCountInLayer/getComponentsInLayer`。

## 2. Java 用户如何使用
典型组合：`new JLayeredPane()` → `add(palette, PALETTE_LAYER)` → `add(content, DEFAULT_LAYER)`；需要置顶时 `moveToFront(comp)`；查询层级 `getLayer(comp)`、层级内组件 `getComponentsInLayer(layer)`。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/swing/JInternalFrame/bug4151444.java`、`bug4215380.java` 与 `JRootPane/RootPaneDecorationSize.java`：通过 JLayeredPane 组装内部框架/根窗格层级。
- JDK 源码 `javax.swing.JRootPane`：使用 FRAME_CONTENT_LAYER、DEFAULT_LAYER、PALETTE_LAYER 组织内容/菜单栏/弹出层，是 JLayeredPane 的典型自用。
- 常见拖拽编辑器用 `moveToFront` + `setLayer` 管理对象重叠层级。

## 4. 测试用例设计 + 自动化实现
黑盒覆盖：全部层级常量精确值；默认 null 布局、optimizedDrawing、空 pane 查询；`add` 不同层级的 getLayer/getIndexOf/highestLayer/lowestLayer/getComponentCountInLayer/getComponentsInLayer；`setLayer` 迁移层级后的顺序与数量；`moveToFront/setPosition/getPosition` 顺序；`remove` 后的索引与残留 layer 属性；静态 `putLayer/getLayer` 与 `getLayeredPaneAbove` 命中/null。见 `JLayeredPaneTest.java` 的 `testBlackBox()`。

# java.awt.Component 黑盒行为测试设计

## ① API 功能分组
- 名称/父子：`getName/setName/getParent`。
- 状态：`setEnabled/isEnabled/enable/disable`、`setVisible/isVisible/show/hide/isShowing/isDisplayable`。
- 颜色字体：`setBackground/getBackground/isBackgroundSet`、`setForeground/getForeground`、`setFont/getFont`、`setLocale/getLocale`。
- 几何：`setSize/getSize/resize`、`setBounds/getBounds/getX/getY`、`setLocation/getLocation/move`、`contains/inside`。
- 尺寸提示：`setPreferredSize/getPreferredSize/isPreferredSizeSet` 及 min/max 三组。
- 光标/输入：`setCursor/getCursor/isCursorSet`、`setIgnoreRepaint/getIgnoreRepaint`、`setComponentOrientation/getComponentOrientation`、`setFocusable/isFocusable`、`getAlignmentX/Y`。
- 工具：`isLightweight/isOpaque/getTreeLock/getToolkit/getGraphicsConfiguration/toString`。

## ② Java 用户用法
```java
Component c = new Canvas();
c.setName("w"); c.setEnabled(true); c.setBackground(Color.RED);
c.setBounds(10, 20, 100, 100);
c.setVisible(true);
c.getParent(); c.getSize(); c.getPreferredSize();
```
组合链路：构造子类实例 → add 到显示中的 Frame → 状态/几何/尺寸提示 getter-setter 组合 → 读回断言。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Container.addImpl` 设置子组件 `parent` 字段并触发 addNotify（`Container.java`）。
- `Component.setBounds` 更新 x/y/width/height 并 reshape 原生 peer（`Component.java`）。
- Swing/真实项目大量用 `component.setPreferredSize` + `revalidate` 控制布局。

## ④ 测试用例设计 + 自动化实现
- 对齐常量精确值 → `testBlackBox`。
- add 到显示 Frame 后 `isDisplayable/isShowing/getParent/getGraphicsConfiguration` → `testBlackBox`。
- 名称/使能/颜色/字体/区域/几何/尺寸提示/光标/朝向/可聚焦 组合 getter-setter 往返 → `testBlackBox`。
- 弃用方法 `disable/enable/hide/show/move/resize` → `testBlackBox`。
- `contains/inside` 边界 → `testBlackBox`。

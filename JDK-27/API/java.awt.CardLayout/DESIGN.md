# java.awt.CardLayout 黑盒行为测试设计

## ① API 功能分组
- 构造器：`CardLayout()`、`CardLayout(int hgap, int vgap)`。
- 卡片切换：`first/last/next/previous/show(Container, String)`。
- 约束操作：`addLayoutComponent(Component, Object)`、`removeLayoutComponent`。
- 布局计算：`preferredLayoutSize/minimumLayoutSize/maximumLayoutSize/layoutContainer`。
- 查询：`getHgap/setHgap/getVgap/setVgap`。

## ② Java 用户用法
```java
CardLayout cl = new CardLayout();
panel.setLayout(cl);
panel.add("home", homePanel);
panel.add("settings", settingsPanel);
cl.show(panel, "settings");
```
组合链路：设布局 → 按名字 add 多个卡片 → doLayout 显示默认卡 → show/next/previous 切换可见卡。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `CardLayout.show` 遍历 `vector` 匹配名字，把旧可见卡 `setVisible(false)`、新卡 `setVisible(true)` 并 `validate`（`CardLayout.java`）。
- `CardLayout.layoutContainer` 把所有组件设为相同 bounds（含 hgap/vgap 与 insets）（`CardLayout.java`）。
- `CardLayout.preferredLayoutSize` 取所有卡片的 max pref 宽高加 `hgap*2/vgap*2`（`CardLayout.java`）。

## ④ 测试用例设计 + 自动化实现
- 构造器/间隙 getter-setter 往返 → `testBlackBox`。
- 三卡片 doLayout 显示第一张 + 精确 bounds → `testBlackBox`。
- `show/next/previous/last/first` 切换可见性 → `testBlackBox`。
- `preferredLayoutSize/minimumLayoutSize` 取 max → `testBlackBox`。
- 带间隙的卡片 bounds 与 preferred 计算 → `testBlackBox`。
- `removeLayoutComponent` 移除卡片 → `testBlackBox`。

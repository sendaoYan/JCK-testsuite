# java.awt.GridBagLayout 黑盒行为测试设计

## ① API 功能分组
- 约束管理：`setConstraints(Component, GridBagConstraints)`、`getConstraints(Component)`、`lookupConstraints`。
- 布局计算：`preferredLayoutSize/minimumLayoutSize/maximumLayoutSize/layoutContainer`。
- 查询：`getLayoutOrigin/getLayoutDimensions/getLayoutWeights/location(int,int)`。
- 公开字段：`columnWidths/rowHeights/columnWeights/rowWeights`。

## ② Java 用户用法
```java
GridBagLayout g = new GridBagLayout();
panel.setLayout(g);
GridBagConstraints c = new GridBagConstraints();
c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
panel.add(comp, c);
panel.validate();
```
组合链路：设布局 → 构造 GridBagConstraints → add 带约束组件 → doLayout 计算网格 → 读回 bounds/尺寸/权重。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `GridBagLayout.setConstraints` 将约束 clone 后存入 `comptable`（`GridBagLayout.java`）。
- `GridBagLayout.getConstraints` 对未知组件先存默认约束再返回 clone（`GridBagLayout.java`）。
- `GridBagLayout.location` 依据 `layoutInfo.minWidth/minHeight` 反查 cell 坐标（`GridBagLayout.java`）。

## ④ 测试用例设计 + 自动化实现
- `setConstraints` 存 clone、`getConstraints` 返 clone、未知组件默认约束 → `testBlackBox`。
- 单组件 weight=1 fill=BOTH 撑满容器 + 尺寸/权重查询 → `testBlackBox`。
- 两行组件 `HORIZONTAL`+`BOTH` 精确 bounds + `location()` 反查 → `testBlackBox`。
- `anchor=NORTHWEST` 保持 preferred 尺寸并居中 → `testBlackBox`。
- `preferredLayoutSize` 计算 → `testBlackBox`。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：四字段可变；两格权重像素级；单格垂直居中；getConstraints 副本；布局信息三元组；默认约束居中；字符串 IAE；alignment/maxdim。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。

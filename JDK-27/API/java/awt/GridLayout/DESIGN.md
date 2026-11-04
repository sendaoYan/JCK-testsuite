# java.awt.GridLayout 黑盒行为测试设计

## ① API 功能分组
- 构造器：`GridLayout()`、`GridLayout(int rows, int cols)`、`GridLayout(int rows, int cols, int hgap, int vgap)`。
- 查询/设置：`getRows/setRows`、`getColumns/setColumns`、`getHgap/setHgap`、`getVgap/setVgap`。
- 布局计算：`preferredLayoutSize/minimumLayoutSize/layoutContainer`。

## ② Java 用户用法
```java
Panel p = new Panel(new GridLayout(2, 3, 4, 5));
for (int i = 0; i < 6; i++) p.add(new Button("" + i));
p.validate();
```
组合链路：设行列/间隙 → add 组件 → doLayout 将组件按 `r*cols+c` 顺序放入等大网格。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `ScrollPane` 内部用固定布局，未直接用 GridLayout，但 JDK 测试大量用 `new GridLayout()` 组织按钮（`test/jdk/java/awt`）。
- `GridLayout.layoutContainer` 依据 `rows`/`cols` 计算 `nrows/ncols`，用截断除法分配 `widthOnComponent`（`GridLayout.java`）。
- `GridLayout.preferredLayoutSize` 取所有组件 pref 的最大宽/高乘行列数（`GridLayout.java`）。

## ④ 测试用例设计 + 自动化实现
- 构造器/行列/间隙 getter-setter 往返 → `testBlackBox`。
- `preferredLayoutSize`（pref）与 `minimumLayoutSize`（min）精确计算 → `testBlackBox`。
- `layoutContainer` 6 组件 2x3 网格每个 cell 精确 bounds → `testBlackBox`。
- 单组件 1x1 网格 preferred 计算 → `testBlackBox`。

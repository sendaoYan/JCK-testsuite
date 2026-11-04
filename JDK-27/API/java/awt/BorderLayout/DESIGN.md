# java.awt.BorderLayout 黑盒行为测试设计

## ① API 功能分组
- 构造器：`BorderLayout()`、`BorderLayout(int hgap, int vgap)`。
- 常量：`NORTH/SOUTH/EAST/WEST/CENTER` 及相对页定位 `PAGE_START/PAGE_END/LINE_START/LINE_END` 等。
- 约束操作：`addLayoutComponent(Component, Object)`、`addLayoutComponent(String, Component)`、`removeLayoutComponent`、`getLayoutComponent`、`getConstraints`。
- 布局计算：`preferredLayoutSize/minimumLayoutSize/maximumLayoutSize/layoutContainer`。
- 查询：`getHgap/setHgap/getVgap/setVgap`。

## ② Java 用户用法
```java
panel.setLayout(new BorderLayout(4, 4));
panel.add(new Button("North"), BorderLayout.NORTH);
panel.add(new Button("Center"), BorderLayout.CENTER);
panel.validate();
```
组合链路：设布局 → 按约束 add 五个区域组件 → doLayout 计算 bounds → 读回组件 getBounds。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Dialog`/`Frame` 等窗口常用 `setLayout(new BorderLayout())` 组织内容（`java.awt` 内部）。
- `Container.add(Component, Object)` 将约束转发给 `LayoutManager2.addLayoutComponent`（`Container.java`）。
- `BorderLayout.layoutContainer` 先北后南再东西最后 center 计算 bounds（`BorderLayout.java`）。

## ④ 测试用例设计 + 自动化实现
- 构造器/间隙 getter-setter 往返 → `testBlackBox`。
- 五区域 add 后 `layoutContainer` 精确 bounds（固定 preferredSize 的 Box 子类）→ `testBlackBox`。
- `getLayoutComponent`/`getConstraints` 查询 → `testBlackBox`。
- `preferredLayoutSize`（用 pref）与 `minimumLayoutSize`（用 min）精确计算 → `testBlackBox`。
- `removeLayoutComponent` 后布局移除 → `testBlackBox`。


## 5. 深化补充（testContract，JDK-8+JDK-27 双侧对齐）
本任务在保留原有断言基础上追加契约级深化：
testContract：五区域像素级；hgap/vgap 变体；N/S 钳制；prefdim (30,40)/(50,90)；未挂载 (0,0)；null=CENTER；IAE 消息逐字；null 移除 NPE；alignment。
全部为 headless（env -u DISPLAY）实测锁定值，双端 JDK 行为一致。

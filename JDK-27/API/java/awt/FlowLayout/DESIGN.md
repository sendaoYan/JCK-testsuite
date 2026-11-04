# java.awt.FlowLayout 黑盒行为测试设计

## ① API 功能分组
- 常量：`LEFT/CENTER/RIGHT/LEADING/TRAILING`。
- 构造器：`FlowLayout()`、`FlowLayout(int align)`、`FlowLayout(int align, int hgap, int vgap)`。
- 查询/设置：`getAlignment/setAlignment`、`getHgap/setHgap`、`getVgap/setVgap`、`getAlignOnBaseline/setAlignOnBaseline`。
- 布局计算：`preferredLayoutSize/minimumLayoutSize/layoutContainer`。

## ② Java 用户用法
```java
Panel p = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 10));
p.add(new Button("a")); p.add(new Button("b"));
p.validate();
```
组合链路：设对齐/间隙 → add 组件 → doLayout 依可用宽度自动换行 → 读回 bounds。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Panel` 默认布局即 `new FlowLayout()`（`Panel.java`）。
- `FlowLayout.layoutContainer` 按 `maxwidth` 判断是否换行并调用 `moveComponents` 对齐（`FlowLayout.java`）。
- `FlowLayout.preferredLayoutSize` 累加可见组件 preferred 宽度并加 `hgap*2`（`FlowLayout.java`）。

## ④ 测试用例设计 + 自动化实现
- 常量精确值 + 构造器/间隙 getter-setter 往返 → `testBlackBox`。
- `preferredLayoutSize`（用 pref）与 `minimumLayoutSize`（用 min）精确计算 → `testBlackBox`。
- `layoutContainer` 不换行时两组件精确 bounds → `testBlackBox`。
- 容器宽度不足时第二组件换行到下行的精确 bounds → `testBlackBox`。

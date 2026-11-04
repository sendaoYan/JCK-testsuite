# javax.swing.OverlayLayout 测试设计

## 1. API 功能
把所有子组件叠放在同一矩形的 LayoutManager2：容器三尺寸 = 最大的子组件 max；
每个子组件按自身 alignment 在容器内定位，实现 JLayeredPane 式覆盖。

## 2. Java 用户如何使用
`JPanel p = new JPanel(new OverlayLayout(p)); p.add(iconLabel); p.add(basePanel);`
子组件各自 `setAlignmentX/Y` 控制停靠位置。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk/src/java.desktop/share/classes/javax/swing/OverlayLayout.java`（checkContainer/addLayoutComponent 为
invalidateLayout(parent) 委托）；JLayeredPane/OptionPane 相关测试。

## 4. 测试用例设计 + 易错细节
- 两个 rigid 10x10/20x20 子组件：容器 pref=min=max 全为 20x20（取子组件 max 包络）。
- 0.5 对齐：10x10 子组件居中 (5,5)，20x20 子组件铺满 (0,0)。
- **对齐反转易错**：定位公式是 `x = 容器对齐*W - 子对齐*childW`，子组件 alignmentX=0 反而落到
  x=10（右侧），alignmentX=1 落到 x=0——直觉的"0=靠左"不成立，逐值锁定。
- 子组件高度被拉伸到容器高（max 允许时）：10x10 的 JPanel 子项变 (10,0,10,20)。
- getLayoutAlignmentX/Y 由子组件推导：0.5 子项 → 0.5；单独 JLabel（对齐 0）→ 0.0。
- **addLayoutComponent(String, comp) 对孤儿组件抛 `AWTError("OverlayLayout can't be shared")`**
  （invalidateLayout(null) → checkContainer），对属于目标容器的子组件无副作用；
  layoutContainer/preferredLayoutSize/minimumLayoutSize 传入其他容器同样抛该 AWTError。

自动化实现位于同目录 `*Test.java`：checkThrows 精确校验 AWTError 类型与消息文本。

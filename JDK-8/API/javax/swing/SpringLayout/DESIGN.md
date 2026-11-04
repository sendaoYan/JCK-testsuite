# javax.swing.SpringLayout 测试设计

## 1. API 功能
基于边（edge）约束的 LayoutManager2：9 个字符串边名（North/South/East/West/HorizontalCenter/
VerticalCenter/Baseline/Width/Height），putConstraint 建立边到边的偏移，Constraints 对象直接持有
x/y/width/height 弹簧，EAST/SOUTH 为派生弹簧。

## 2. Java 用户如何使用
`SpringLayout l = new SpringLayout(); p.setLayout(l);`
`l.putConstraint(SpringLayout.WEST, child, 5, SpringLayout.WEST, p);`
或 `l.getConstraints(child).setX(Spring.constant(1))`。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk/src/java.desktop/share/classes/javax/swing/SpringLayout.java`（Constraints.getConstraint
返回 width/height 派生弹簧）；`test/jdk/javax/swing/SpringLayout/`。

## 4. 测试用例设计 + 易错细节
- 9 个边名逐串锁定（"North"/"HorizontalCenter"/"Width" 等，首字母大写的合成词）。
- **无参 Constraints 的四个 getter 全为 null**（不是 UNSET 弹簧）——new SpringLayout.Constraints()
  后直接 getX().getValue() 会 NPE；组件版构造器从组件尺寸播种 width/height。
- putConstraint(WEST,5)/(NORTH,7)：getConstraint 值 5/7；**容器 min/pref 仍是 0x0**——只锚
  WEST/NORTH 不会推导容器尺寸，必须再锚 EAST/SOUTH 或 width/height。
- 固定尺寸 JButton(40x12) 消除字体差异：布局后 (5,7,40,12) 精确锁定。
- 显式 setX(1)/setY(2)/setWidth(40)/setHeight(12)：**getConstraint(EAST)==41、SOUTH==14
  （派生 = 基边 + 长度）**；getConstraint(WEST,c) 与 constraints.getX() **不是同一实例**
  （每次调用返回新的派生弹簧）。
- getConstraints(Component) 每次返回同一 Constraints 对象（按组件缓存）。
- getLayoutAlignmentX/Y 恒 0.5；add/remove/invalidate 均无副作用。

自动化实现位于同目录 `*Test.java`。

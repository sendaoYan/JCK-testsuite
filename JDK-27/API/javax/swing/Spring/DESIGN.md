# javax.swing.Spring 测试设计

## 1. API 功能
抽象 min/pref/max 三值弹簧 + 代数组合子：constant/sum/minus/max/scale/width/height，
供 SpringLayout 做约束式布局；UNSET = Integer.MIN_VALUE 表示未赋值。

## 2. Java 用户如何使用
`Spring.width(c)` 取组件宽度弹簧；`Spring.sum(a, b)`、`Spring.scale(s, 0.5f)` 组合后塞进
`SpringLayout.Constraints.setX/setWidth`；setValue 驱动布局求解。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk/src/java.desktop/share/classes/javax/swing/Spring.java`（负数因子、Math.round 舍入）；
SpringLayout.java 内部对 UNSET 的处理。

## 4. 测试用例设计 + 易错细节
- UNSET == Integer.MIN_VALUE 精确锁定；constant(5) 三值全 5。
- **setValue 不做钳制**：range(2,6,10).setValue(99) → getValue()==99（数值可以越过 max）。
- 组合弹簧的三值从操作数三值逐项推导：sum(2/6/10, 1/3/5) → 3/9/15；minus 反转区间 -10/-6/-2；
  max 逐值取大 (2,6,20)；scale(2/4/6, 0.5f) → 1/2/3。
- **scale 半数向上取整**：scale(7, 0.5f)==4（Math.round(3.5)）；负因子合法：scale(7, -1f)==-7。
- width/height 与组件三尺寸一一对应（3/25/40 ↔ 5/15/50）；**未显示组件只设 preferredSize 时
  min/max 都是 0（0/25/0）**——JLabel 不像 JPanel 会回填 min/max，勿断言非零。
- width/height 返回的是派生弹簧（跟随组件变化），非快照。

自动化实现位于同目录 `*Test.java`。

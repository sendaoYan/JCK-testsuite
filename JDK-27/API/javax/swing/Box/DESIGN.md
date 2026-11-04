# javax.swing.Box 测试设计

## 1. API 功能
内置 BoxLayout 的轻量容器 + 一组生成不可见填充组件的工厂：createRigidArea（三尺寸固定）、
createHorizontal/VerticalStrut（单轴固定、max=32767）、createGlue/createHorizontal/VerticalGlue
（min=pref=0、max=32767 的弹性填充）、Box.Filler（显式三尺寸 + changeShape）。

## 2. Java 用户如何使用
`Box row = Box.createHorizontalBox(); row.add(field); row.add(Box.createHorizontalStrut(8)); row.add(button);`
用 struts 表固定间距、glue 表弹性间距，替代手工 GridBag 约束。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk/src/java.desktop/share/classes/javax/swing/Box.java`（Filler.changeShape 与 Accessible 实现）；
Swing 自身 JOptionPane/JToolBar 中的 glue/strut 用例。

## 4. 测试用例设计 + 易错细节
- createHorizontalBox/VerticalBox 的布局类是 BoxLayout 且 getAxis 为 0/1；Box(axis) 构造器同样。
- rigid：min=pref=max=(10,10) 三者全等（只断言 pref 会漏掉这层契约）。
- hstrut(7)：min=pref=(7,0)，**max=(7,32767)**——strut 的 max 两轴都是 Short.MAX_VALUE，
  纵向因此在 BoxLayout 里可被拉满；vstrut 镜像。
- glue 家族：hglue max=(32767,0)，vglue max=(0,32767)，createGlue max=(32767,32767)，min/pref 全 0。
- Box.Filler(3x4,5x6,7x8)：三尺寸分别生效；setBounds(9,9) 不改声明尺寸；changeShape 一次性换三组。
- 组合断言：rigid+glue+rigid 在 100 宽 → 0 与 90；vglue 在 50 高 → 底部 y=45。
- Box 与 Box.Filler 均实现 Accessible（getAccessibleContext 非 null）。

自动化实现位于同目录 `*Test.java`。

# javax.swing.BoxLayout 测试设计

## 1. API 功能
多个组件沿单轴（X/Y/LINE/PAGE）依 min/pref/max 弹性排布的 LayoutManager2：
X 轴子项依次铺开并拉伸到容器全高，Y 轴镜像；对 glue/strut（0 与 32767 的特殊尺寸）做弹性分配。

## 2. Java 用户如何使用
`JPanel p = new JPanel(new BoxLayout(p, BoxLayout.Y_AXIS)); p.add(a); p.add(b);`
或 `Box.createHorizontalBox()`；配合 `Box.createRigidArea/createHorizontalStrut/createHorizontalGlue`
构造固定间距与弹性间距。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk/src/java.desktop/share/classes/javax/swing/BoxLayout.java`（checkRequests/checkContainer）；
`Box.java`（Filler 三尺寸语义）；`test/jdk/javax/swing/`。

## 4. 测试用例设计 + 易错细节
- 轴常量 0/1/2/3 精确锁定；`getTarget/getAxis` 返回构造参数。
- **`new BoxLayout(c, 99)` 抛 `AWTError("Invalid axis")`（非 IllegalArgumentException）**；`new BoxLayout(null, X)` 不抛。
- 逐值锁定几何：rigid 10/20/30 在 60x10 → x=0,10,30，宽 10/20/30；纵向 rigid 高 5/7 → y=0,5；
  pref/min/max = (60,10)/(10,12)（rigid 三尺寸相等时聚合尺寸即元素和/最大高度）。
- glue 吸收全部富余：rigid+glue+rigid 在 100 宽 → 第二个贴到 x=90；纵向 glue 底部子项 y=45（50 高）。
- strut 只贡献固定宽度：5+hstrut(7)+5 → 第二个在 x=12。
- **LINE_AXIS 在 LTR ComponentOrientation 下与 X_AXIS 完全同构**（含纵向居中 y=2/1 的取整）。
- alignment 缓存请求后两轴均 0.5（空容器时是 0——子项驱动，勿在空容器上断言）。
- **addLayoutComponent(String, comp) 不是 no-op 也不是抛错入口：它调用
  invalidateLayout(comp.getParent())，孤儿组件（parent 为 null）经 checkContainer 触发
  `AWTError("BoxLayout can't be shared")`**；传"属于目标容器的子组件"则无副作用。
- layoutContainer/preferredLayoutSize/minimumLayoutSize/getLayoutAlignmentY 传入其他容器同样
  `AWTError("BoxLayout can't be shared")`（布局与容器一对一绑定）。
- hstrut 单独成箱：pref (7,0)，max (7,32767)——strut 的最大尺寸两轴都是 Short.MAX_VALUE。

自动化实现位于同目录 `*Test.java`：checkThrows 精确校验 AWTError 类型与消息文本。

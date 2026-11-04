# javax.swing.GroupLayout 测试设计

## 1. API 功能
分层组合布局（BoxLayout 之父）：SequentialGroup 顺序铺开、ParallelGroup（LEADING/TRAILING/
CENTER/BASELINE）共享区间；linkSize 强制等尺寸、replace 原地换件、setAutoCreateGaps/ContainerGaps
自动经 LayoutStyle 插间距，setLayoutStyle 注入自定义间距源。

## 2. Java 用户如何使用
GUI 构建器主推：`GroupLayout g = new GroupLayout(panel); panel.setLayout(g);`
`g.setHorizontalGroup(g.createSequentialGroup().addComponent(l).addComponent(f));`
`g.setVerticalGroup(g.createParallelGroup(BASELINE).addComponent(l).addComponent(f));`

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk/src/java.desktop/share/classes/javax/swing/GroupLayout.java`（linkSize 轴校验、replace）；
`test/jdk/javax/swing/GroupLayout/`。

## 4. 测试用例设计 + 易错细节
- **getLayoutStyle 默认 null；setLayoutStyle(null) 真的清成 null**（与 LayoutStyle.setInstance(null)
  复位语义相反，二者勿混淆）。
- **linkSize(-1=DEFAULT_SIZE, ...) 抛 IAE "Axis must be one of SwingConstants.HORIZONTAL or
  SwingConstants.VERTICAL"**（注意 GroupLayout 合法轴只有 SwingConstants.HORIZONTAL/VERTICAL）。
- rigid 子项消除字体差异后逐值锁定：seq(40, gap5, 60) → (0,0,40,10)/(45,0,60,12)，pref=min=105x12；
  par LEADING 横 + seq 纵 → x 全 0、y=0/10。
- BASELINE 平行组中 rigid 项共享 y=0（基线重合），组高取最大子项 12。
- linkSize(w1,w2) 等宽到较大者 70，第二个起点 x=70；带轴版本 HORIZONTAL/VERTICAL 均不抛。
- replace(e1,e2)：e2 进入容器且 e1 移除，随后 doLayout e2=(0,0,25,10)——组内引用同步换绑。
- autoCreateGaps/autoCreateContainerGaps 默认 false、setHonorsVisibility(false) roundtrip。

自动化实现位于同目录 `*Test.java`。

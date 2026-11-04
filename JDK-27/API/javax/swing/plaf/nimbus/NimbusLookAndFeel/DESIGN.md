# javax.swing.plaf.nimbus.NimbusLookAndFeel — DESIGN

## 1. API 功能
`NimbusLookAndFeel extends SynthLookAndFeel`（public 普通类，非抽象，无接口），Nimbus 皮肤 LAF。实例 `initialize`/`uninitialize`、`register(Region,String)`、`getDisabledIcon(JComponent, Icon)`、`getDerivedColor(...)`；静态 `getStyle(JComponent, Region)`；覆写 `getName`/`getID`/`getDescription`。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/NimbusLookAndFeel.java`（class=65、initialize=105、getStyle=230、getName=240、getID=250、getDescription=259、register=347、getDisabledIcon=352、getDerivedColor=379）。

## 2. Java 用户如何使用
`UIManager.setLookAndFeel(new NimbusLookAndFeel())` 安装；`NimbusLookAndFeel.getStyle(comp, Region.BUTTON)` 取样式；`laf.getDerivedColor("nimbusBase", ...)` 派生颜色。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/NimbusLookAndFeel.java:230`：`getStyle` 委托 `getDefaults().getStyle`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/NimbusLookAndFeel.java:352`：`getDisabledIcon` 生成灰阶禁用图标。

## 4. 测试用例设计（黑盒部分，`NimbusLookAndFeelTest.testBlackBox`）
1. 安装后 `getName()=="Nimbus"`、`getID()=="Nimbus"`、`getDescription()=="Nimbus Look and Feel"`、`isNative==false`、`isSupported==true`。
2. `getDefaults()` 非空。
3. `getDerivedColor` 非空。
4. `getDisabledIcon` 返回非原图标的派生图标。
5. `register` 自定义 Region 完成；`getStyle(JButton, Region.BUTTON)` 返回 `NimbusStyle`。
6. finally `uninitialize` 并恢复原 style factory。

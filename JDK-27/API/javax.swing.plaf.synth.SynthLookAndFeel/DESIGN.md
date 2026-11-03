# javax.swing.plaf.synth.SynthLookAndFeel — DESIGN

## 1. API 功能
`SynthLookAndFeel extends BasicLookAndFeel`（public 普通类，非抽象，无接口），Synth 可设样式 LAF。静态工厂 `setStyleFactory`/`getStyleFactory`、`getStyle`、`getRegion`、`createUI`、`updateStyles`；实例 `load`/`initialize`/`uninitialize`。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthLookAndFeel.java`（class=97、setStyleFactory=211、getStyleFactory=227、getRegion=341、createUI=429、initialize=613）。

## 2. Java 用户如何使用
`SynthLookAndFeel.setStyleFactory(factory)` 注入样式工厂；`SynthLookAndFeel.createUI(comp)` 创建对应 UI；`SynthLookAndFeel.getRegion(comp)` 取组件 Region。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthLookAndFeel.java:211`：`setStyleFactory` 写入 AppContext。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthLookAndFeel.java:429`：`createUI` 按 UI class id 反射加载 `SynthXUI`。

## 4. 测试用例设计（黑盒部分，`SynthLookAndFeelTest.testBlackBox`）
1. `getName()=="Synth look and feel"`、`getID()=="Synth"`、`getDescription()` 同 getName、`isNativeLookAndFeel()==false`、`isSupportedLookAndFeel()==true`。
2. `getDefaults()` 非空。
3. `createUI(JButton)` 返回 `SynthButtonUI`；`getRegion(JButton)==Region.BUTTON`。
4. `setStyleFactory`/`getStyleFactory` 往返一致，finally 恢复。

# javax.swing.plaf.synth.SynthStyleFactory — DESIGN

## 1. API 功能
抽象类，父类 Object，无接口。唯一抽象方法：`public abstract SynthStyle getStyle(JComponent c, Region id)`——按组件与 Region 返回 SynthStyle。

## 2. Java 用户如何使用
子类化并实现 `getStyle`，再交给 `SynthLookAndFeel.setStyleFactory(factory)`，Synth*UI 的 `updateStyle` 会调用 `SynthLookAndFeel.getStyleFactory().getStyle(c, region)` 取得样式。

## 3. 开源用法调研（来源：jdk25u-dev 源码 + test/jdk）
1. `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthLookAndFeel.java:211-235`：`setStyleFactory` 把工厂存入 AppContext，`getStyleFactory` 读取。
2. `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/DefaultSynthStyleFactory.java`：JDK 内置实现，按组件名/Region 正则匹配并合并样式。
3. `~/git/jdk25u-dev/test/jdk/javax/swing/plaf/gtk/TestCustomStyleFactory.java:64-68`：`SynthLookAndFeel.setStyleFactory(new SynthStyleFactory() { public SynthStyle getStyle(JComponent c, Region id){...} })`。

## 4. 测试用例设计（黑盒部分，`SynthStyleFactoryTest.testBlackBox`）
匿名子类固定返回同一个 SynthStyle，断言 `getStyle(null, Region.BUTTON)` 与 `getStyle(null, Region.SLIDER)` 都返回该实例（工厂分派语义）。

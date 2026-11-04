# javax.swing.plaf.nimbus.NimbusStyle — DESIGN

## 1. API 功能
`NimbusStyle extends SynthStyle`（public final 类，非抽象，无接口），Nimbus 皮肤样式实现。覆写 `getInsets`/`getPainter`/`isOpaque`/`installDefaults`/`get` 等；继承 `getColor`/`getFont`；常量 `LARGE_KEY`/`SMALL_KEY`/`MINI_KEY` 与缩放因子。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/NimbusStyle.java`（class=120、getInsets=487、getPainter=605、isOpaque=616、get=658）。

## 2. Java 用户如何使用
`NimbusLookAndFeel.getStyle(comp, Region)` 获取后，以 `SynthContext` 调用 `getColor(ctx, ColorType.BACKGROUND)`、`getFont(ctx)`、`getInsets(ctx, ins)`、`getPainter(ctx)`、`isOpaque(ctx)`。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/NimbusStyle.java:616`：`isOpaque` 依据内部 opaque 属性（按钮默认 false）。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/nimbus/NimbusStyle.java:605`：`getPainter` 返回区域背景/前景画家。

## 4. 测试用例设计（黑盒部分，`NimbusStyleTest.testBlackBox`）
需先 `UIManager.setLookAndFeel(new NimbusLookAndFeel())`（validate() 会把 UIManager LAF 强转为 NimbusLookAndFeel）：
1. `getStyle(JButton, Region.BUTTON)` 返回 `NimbusStyle`。
2. `getColor(ctx, BACKGROUND/FOREGROUND/FOCUS)` 与 `getFont(ctx)` 非空。
3. `getInsets(ctx, ins)` 非空。
4. `getGraphicsUtils(ctx)`、`getPainter(ctx)`、`getBackgroundPainter(ctx)` 非空。
5. `isOpaque(ctx) == false`（按钮）。
6. finally 恢复原 LAF。

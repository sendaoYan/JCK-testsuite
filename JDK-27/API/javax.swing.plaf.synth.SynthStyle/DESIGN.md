# javax.swing.plaf.synth.SynthStyle — DESIGN

## 1. API 功能
抽象类，父类 Object，无接口。唯一两个抽象方法：`protected abstract Color getColorForState(SynthContext, ColorType)`、`protected abstract Font getFontForState(SynthContext)`。其余为具体方法：
- 查询：`getColor/getFont/getInsets/getPainter/isOpaque/get/getInt/getBoolean/getIcon/getString/getGraphicsUtils`。
- 默认值：静态 `DEFAULT_VALUES` 表（如 `Table.dropLineColor=BLACK`、`Table.dropLineShortColor=RED`），由 `get(Object key)` 查表。
- 安装/卸载：`installDefaults(SynthContext)`、`uninstallDefaults(SynthContext)`。

## 2. Java 用户如何使用
子类化并实现两个抽象方法后，通过 `new SynthContext(component, region, style, SynthConstants.ENABLED)` 构造上下文，再调用 `style.getColor(ctx, ColorType.BACKGROUND)`、`style.getFont(ctx)`、`style.getInt(ctx, key, def)` 等。`getGraphicsUtils(ctx)` 返回共享 `SynthGraphicsUtils`；`isOpaque(ctx)` 默认 true；`getPainter(ctx)` 默认 null。

## 3. 开源用法调研（来源：jdk25u-dev 源码 + test/jdk）
1. `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthContext.java`：`getStyle()/getPainter()` 直接消费 SynthStyle。
2. `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthLookAndFeel.java:287-352`：`SynthStyle.updateStyle(context, ui)` 与 `getStyleFactory().getStyle(...)` 产出/更换 SynthStyle。
3. `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/DefaultSynthStyleFactory.java:getStyle(...)`：无匹配时返回默认 `DefaultSynthStyle`。
4. `~/git/jdk25u-dev/test/jdk/javax/swing/plaf/gtk/TestCustomStyleFactory.java:64-68`：`SynthLookAndFeel.setStyleFactory(new SynthStyleFactory() { getStyle(...){...} })` 定制样式来源。

## 4. 测试用例设计（黑盒部分，`SynthStyleTest.testBlackBox`）
用匿名子类实现两个抽象方法（getColorForState 返回 RED、getFontForState 返回 DIALOG 12），构造 JPanel + SynthContext，断言：
- `getGraphicsUtils(ctx)` 非空；`getColor(ctx,BACKGROUND)` 委派到 getColorForState 返回 RED；`getFont(ctx)` 非空。
- `getInsets(ctx,insets)` 归零；`getPainter(ctx)==null`；`isOpaque(ctx)==true`。
- `get/getInt/getBoolean/getIcon/getString` 对未知 key 走默认值。
- 内建默认值 `Table.dropLineColor==BLACK`、`Table.dropLineShortColor==RED`。

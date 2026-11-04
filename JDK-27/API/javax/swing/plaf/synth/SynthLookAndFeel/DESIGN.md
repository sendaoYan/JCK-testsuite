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

---

## 契约深化（本轮追加，`SynthLookAndFeelContractTest`，56 断言）

- **元数据精确**：getName/getDescription "Synth look and feel"、getID "Synth"、supported true、native false、无窗口装饰、instanceof BasicLookAndFeel。
- **load(InputStream,Class) 契约**：load(null, Class) IAE；load(stream, null) IAE；load(非法 XML) **java.text.ParseException**；load("<synth></synth>") 两次接受。
- **安装态**：最小 synth 文档安装后按钮/标签委托 instanceof SynthUI 且类名 SynthButtonUI；SynthButtonUI.createUI 反射存在并返回 ComponentUI；结束恢复 Metal。
- **ColorType**：FOREGROUND..FOCUS 的 getID() 精确 0/1/2/3/4 + toString + 互异。
- **SynthConstants**：ENABLED/MOUSE_OVER/PRESSED/DISABLED/FOCUSED/SELECTED/DEFAULT 位值 1/2/4/8/256/512/1024 + OR 组合。
- **SynthContext**：四 getter 往返（region 恒等单例）；null component/region/style 三个 NPE。
- **SynthStyle 最小子类 ProbeStyle**：getColor 路由 getColorForState（RED）；get/getInt/getBoolean/getString 未知键回落默认；getInsets(ctx,null) 分配、getInsets(ctx,reused) 填充复用；getGraphicsUtils 非 null；SynthGraphicsUtils 可实例化。
- **JDK-27 专有 `synth.SynthIcon` 接口**（JDK-8 无，javap 确认）：isInterface；三方法 paintIcon(SynthContext,Graphics,int,int,int,int)/getIconWidth(SynthContext)/getIconHeight(SynthContext)；可赋值给 java.awt.Icon。

### 版本差异（javap 逐包确认）
- synth 包类型面差异唯一：**SynthIcon 仅 JDK-27 存在**（JDK-9 引入）。JDK-8 侧对应测试无此节。

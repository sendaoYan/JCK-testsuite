# javax.swing.plaf.synth.SynthLookAndFeel（synth 聚合契约）— DESIGN

## 1. API 功能
`SynthLookAndFeelContractTest` 覆盖 synth 包核心对象：SynthLookAndFeel 身份与 load(InputStream,Class) 校验、ColorType/SynthConstants 值表、SynthContext 构造与 null 校验、SynthStyle 默认解析。（JDK-27 侧另含 SynthIcon 接口；Region 专测见 Region/DESIGN.md。）

## 2. Java 用户如何使用
`SynthLookAndFeel lf = new SynthLookAndFeel(); lf.load(xmlStream, MyPanel.class); UIManager.setLookAndFeel(lf)`；样式经 SynthStyleFactory→SynthStyle→SynthContext 链解析；ColorType 区分前景/背景/焦点等 5 类颜色；SynthConstants 是组件状态位标志。

## 3. 开源用法调研
- `jdk8u-dev/.../synth/SynthLookAndFeel.java:563`：load(InputStream,Class)——resourceBase==null 抛 IAE("You must supply a valid resource base Class")；input==null 走 IAE；非法 XML → ParseException（实测双侧）。
- `SynthLookAndFeel.java:726-747`：getName/getDescription "Synth look and feel"、getID "Synth"。
- `ColorType.java:67-89`：FOREGROUND..FOCUS 五常量；getID 为构造序号（0..4）。
- `SynthContext.java`：null component/region/style 均 NPE（实现前置 Objects.requireNonNull 级检查）。
- JDK-9+ 新增 `synth.SynthIcon` 接口（paintIcon(SynthContext,Graphics,int,int,int,int)/getIconWidth(SynthContext)/getIconHeight(SynthContext)），JDK-8 无。

## 4. 测试用例设计 + 自动化实现（51 断言；JDK-27 侧 56）
- 元数据：name/ID/描述精确、supported true、native false、无窗口装饰、instanceof BasicLookAndFeel、toString 含 Synth。
- load 契约：load(null,Class) IAE；load(stream,null) IAE；load(非法 XML) ParseException；load("<synth></synth>") 两次均接受。
- 安装态：load 最小文档并安装后，按钮/标签委托 instanceof SynthUI 且类名 SynthButtonUI；SynthButtonUI.createUI 反射存在；结束恢复 Metal。
- ColorType：五常量 id 精确（0/1/2/3/4）+ toString + 互不相同。
- SynthConstants：七常量位值精确（1/2/4/8/256/512/1024）+ OR 组合。
- SynthContext：四 getter 往返（region 恒等单例）；null component/region/style 三 NPE。
- SynthStyle（最小子类 ProbeStyle）：getColor 路由 getColorForState（RED）；get/getInt/getBoolean/getString 未知键回落默认；getInsets(ctx,null) 分配新对象、getInsets(ctx,reused) 填充复用；getGraphicsUtils 非 null；SynthGraphicsUtils 可实例化。
- （仅 JDK-27）SynthIcon 接口：isInterface、三方法签名、可赋值给 Icon。

## 5. 易错规范细节
- load 的 IAE 消息文本两侧不同（JDK-8 提到 "Class or URL"），只断言异常类型。
- SynthStyle 抽象面只有 getColorForState/getFontForState（getPainter 等有默认实现），最小子类勿多实现导致签名冲突（getInsets(SynthContext) 的旧抽象已并入 getInsets(SynthContext,Insets)）。
- 安装 Synth/Nimbus 后必须恢复 Metal，否则同 JVM 后续测试的主题假设失效。

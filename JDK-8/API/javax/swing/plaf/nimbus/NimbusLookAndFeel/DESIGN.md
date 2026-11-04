# javax.swing.plaf.nimbus.NimbusLookAndFeel — DESIGN

## 1. API 功能
Nimbus 外观入口（构建在 Synth 之上，headless 可安装）。深化 `NimbusLookAndFeelTest`：身份元数据、派生色、样式查找与 Region 注册、安装态行为。

## 2. Java 用户如何使用
`UIManager.setLookAndFeel(new NimbusLookAndFeel())`；`getDerivedColor(...)` 生成基于父色的偏移色；`register(Region, prefix)` 重定向区域的样式前缀；`getStyle(JComponent, Region)` 静态查样式（需 Nimbus 已安装）。

## 3. 开源用法调研
- `jdk8u-dev/.../nimbus/NimbusLookAndFeel.java`：getName/getID "Nimbus"、getDescription "Nimbus Look and Feel"；shouldUpdateStyleOnAncestorChanged true。
- Nimbus 委托走 Synth 引擎：安装后 JButton 委托是 SynthButtonUI（instanceof SynthUI），按钮默认不透明 false（由 Region 绘制）。
- 实测：getSupportsWindowDecorations false（Nimbus 自绘 chrome 由 RootPaneUI 处理）。
- getDerivedColor 同参缓存（同一实例），uiResource=false 变体另建实例。

## 4. 测试用例设计 + 自动化实现（50 断言，深化新增 ~33）
- 元数据：五方法精确值 + instanceof SynthLookAndFeel + shouldUpdateStyleOnAncestorChanged true。
- 派生色：getDerivedColor("white",1,1,1,0,true) 非 null、alpha 合法、同参同实例、false 变体新实例。
- 样式/注册：register(BUTTON, "Button[Disabled]") 接受；getDisabledIcon 容忍 headless；安装后 getStyle(按钮,BUTTON) 非 null 且是 NimbusStyle、Label 同。
- 安装态：按钮委托 SynthButtonUI（SynthUI）、opaque false、defaults 表 >100 且含 ButtonUI；结束恢复 Metal+Ocean。
- 保留原白盒 Part（结构断言原文不动）。

## 5. 易错规范细节
- NimbusLookAndFeel.getStyle 静态方法在未安装 Synth/Nimbus 时 NPE（读当前 LaF 的 SynthStyleFactory），必须在安装态断言。
- Nimbus 主题色经 Painter 体系绘制，不要对组件像素做 Nimbus 断言（本批渲染断言只用于 Metal）。

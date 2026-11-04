# javax.swing.plaf.metal.MetalLookAndFeel — DESIGN

## 1. API 功能
Metal 外观入口（headless 可安装）。深化 `MetalLookAndFeelTest`：身份元数据、主题注册表、主题驱动的静态颜色/字体面、安装态行为。

## 2. Java 用户如何使用
`UIManager.setLookAndFeel(new MetalLookAndFeel())` 后组件取 Metal 委托；`MetalLookAndFeel.setCurrentTheme(theme)` 切换 Ocean/Steel（自定义 MetalTheme 亦可）；约 40 个静态 `get*Color()`/6 个 `get*Font()` 读当前主题。

## 3. 开源用法调研
- `jdk8u-dev/.../metal/MetalLookAndFeel.java:178-233`：getName "Metal"、getID "Metal"、isNative false、isSupported true、getSupportsWindowDecorations true。
- `MetalLookAndFeel.java`：setCurrentTheme(null) 抛 NPE("Can't have null theme")；默认主题 OceanTheme（getDefaults 时安装）。
- `DefaultMetalTheme`（Steel）：getControl=(204,204,204)、getControlTextColor=(0,0,0)；`OceanTheme`：getControl=(238,238,238)、getBlack 用 PrintColorUIResource(51,51,51)。
- OpenJDK jtreg `test/javax/swing/plaf/metal/`：主题切换回归。

## 4. 测试用例设计 + 自动化实现（127 断言，深化新增 ~110）
- 元数据：五方法精确值（name/ID "Metal"、描述非空、native false、supported true、窗口装饰 true）。
- 主题注册表：currentTheme 默认 Ocean；Steel/Ocean 名 "Steel"/"Ocean"；DefaultMetalTheme instanceof MetalTheme；setCurrentTheme(null) NPE 且消息精确；切 Steel→getControl(204,204,204)/文字黑→切 Ocean→getControl(238,238,238) 且文字色变化；finally 恢复。
- 静态面：6 字体 getter 非 null FontUIResource 正尺寸；40 颜色 getter 非 null 全不透明；Steel 白(255,255,255)/黑(0,0,0)；Ocean 黑(51,51,51)（打印黑替代纯黑）。
- 安装态：UIManager 安装；defaults 表 >100；ButtonUI→MetalButtonUI；按钮委托 MetalButtonUI；getDisabledIcon/getDisabledSelectedIcon(普通图标) 均返回 null；getLayoutStyle 非 null；provideErrorFeedback(null) 不炸。
- 保留原白盒 Part（结构断言原文不动）。

## 5. 易错规范细节
- 测颜色必须在显式 set 主题后断言：默认 Ocean 的黑色是 (51,51,51) 不是 (0,0,0)。
- getCurrentTheme 在未安装 Metal 时也返回 OceanTheme（静态初始化即建）。
- getControlTextColor 返回 sun.swing.PrintColorUIResource（Metal 内部类型），equals 普通 Color 成立。

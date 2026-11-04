# javax.swing.JColorChooser 测试设计（JDK-8 侧）

## 1. API 功能
颜色选择器：三构造器（WHITE 默认 / Color / ColorSelectionModel）、setColor 三重载、
模型切换、chooserPanels 列表管理、previewPanel、headless showDialog/createDialog。

## 2. Java 用户如何使用
`JColorChooser cc = new JColorChooser(); cc.setColor(Color.RED); cc.addChooserPanel(panel);`
`JColorChooser.showDialog(parent, "Pick", color)`（headless 抛 HeadlessException）。

## 3. 开源代码实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/JColorChooser.java`；JDK 测试 `test/javax/swing/JColorChooser/`；本仓库 probe。

## 4. 测试用例设计 + 易错细节（headless 锁定，Metal LaF + EDT）
- ctor(model) 保留模型身份且颜色跟随模型；默认 DefaultColorSelectionModel、颜色 WHITE。
- setColor(Color/int/r,g,b) 精确通道断言；同色不触发事件、异色恰好 1 个 ChangeEvent；
  setSelectionModel 后 setColor 事件来自新模型监听器。
- 面板规则：Metal 默认 5 个；**add 新实例 → 6；removeChooserPanel 返回被移除的面板（非 boolean）**；
  移除从未加入的面板 → IllegalArgumentException；
  **add 一个已安装的面板实例 → 面板自身抛 RuntimeException（"already installed"）且内部列表先增长了**。
- **JDK-8 版本差异：getChooserPanels() 每次返回同一内部数组引用（别名，可突变破坏内部状态）；
  JDK-27 每次返回防御拷贝**。
- setPreviewPanel(null) → null；默认非 null。
- headless：showDialog/createDialog → HeadlessException；getUIClassID "ColorChooserUI"；
  Metal 下 BasicColorChooserUI；updateUI no-throw。

自动化实现位于同目录 `JColorChooserTest.java`（headless，无 @key）。

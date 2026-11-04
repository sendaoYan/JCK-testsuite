# javax.swing.colorchooser.ColorChooserComponentFactory 测试设计（JDK-8 侧）

## 1. API 功能
静态工厂（public 非 final、唯一构造器 private）：getDefaultChooserPanels()/getPreviewPanel()。

## 2. Java 用户如何使用
不直接使用；BasicColorChooserUI 通过它装配默认面板与预览组件。

## 3. 开源代码实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/colorchooser/ColorChooserComponentFactory.java`（Swatches/HSV/HSL/RGB/CMYK +
DefaultPreviewPanel）。

## 4. 测试用例设计 + 易错细节（headless 锁定，Metal LaF + EDT）
- **类不是 final（与直觉相反），唯一构造器 private（纯静态工厂）**。
- Metal 默认 5 面板，显示名精确锁定 "Swatches;HSV;HSL;RGB;CMYK"；每个面板 mnemonic!=0 且
  displayedMnemonicIndex>=0（具体值不锁，避免 LaF 文案耦合）。
- **getPreviewPanel() 每次返回新实例**，具体类型 DefaultPreviewPanel。
- 面板实例可直接 addChooserPanel 进 JColorChooser（与 chooser 集成）。

自动化实现位于同目录 `ColorChooserComponentFactoryTest.java`（headless，无 @key）。

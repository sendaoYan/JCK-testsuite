# javax.swing.JCheckBox 测试设计

## 1. API 功能
复选框（JToggleButton 子类）：8 个构造器、CheckBoxUI 委托、Metal 下
borderPainted=false 的默认差异、组内互斥、doClick 翻转。

## 2. Java 用户如何使用
`JCheckBox c = new JCheckBox("Enable cache"); c.addItemListener(e -> ...)`；
表单里多个独立布尔开关各自一个实例；强行互斥时也可入 ButtonGroup。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/JCheckBox.java`；`test/jdk/javax/swing/JCheckBox/`；
MetalCheckBoxUI 提供方形勾选图标与边框策略。

## 4. 测试用例设计 + 易错细节
- 8 构造器矩阵（含 `(null, null, true)` 的 null 容忍变体）。
- Metal 默认：**borderPainted=false**（与普通按钮 true 相反）；rolloverEnabled=true；
  focusPainted=true；模型继承 ToggleButtonModel；`CheckBoxUI`/`MetalCheckBoxUI`。
- doClick 翻转 + ItemEvent SELECTED/DESELECTED 各一次。
- 组内互斥与移除语义（移除后成员保留状态）。
- mnemonic 与 text 匹配时 **UI 自动派生 displayedMnemonicIndex**（'C' vs "check" → 0）；
  `setDisplayedMnemonicIndex(-1)` 显式清除。

自动化实现位于同目录 `*Test.java`（headless，Metal LaF + EDT）。

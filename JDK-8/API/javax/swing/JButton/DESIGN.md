# javax.swing.JButton 测试设计

## 1. API 功能
AbstractButton 的具体按下型按钮：5 个构造器（空/Icon/文本/文本+Icon/Action）、
ButtonUI 委托、isDefaultCapable/isDefaultButton（root pane 默认按钮链）、
DefaultButtonModel + setModel、updateUI。

## 2. Java 用户如何使用
`new JButton("OK")` 或 `new JButton(saveAction)`；对话框里
`getRootPane().setDefaultButton(btn)` 让回车触发；doClick 驱动 ActionListener。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/JButton.java`；`test/jdk/javax/swing/JButton/`（含 DefaultButton
相关回归测试）；MetalLookAndFeel 安装 MetalButtonUI。

## 4. 测试用例设计 + 易错细节
- 构造器矩阵：no-arg 的 **text 是 ""**（LaF 的 Button.text 默认值，不是 null），
  actionCommand 随之回退为 ""；Action 构造器同步 NAME/MNEMONIC_KEY/SHORT_DESCRIPTION
  并保存 action 引用。
- UI 身份：`getUIClassID()=="ButtonUI"`；Metal 下 `MetalButtonUI`；
  `UIManager.setLookAndFeel` 重设后已有按钮的 UI 被更新（同一实例不需重建）。
- `isDefaultCapable()==true`；无 root pane 时 `isDefaultButton()==false`。
- 模型：默认 DefaultButtonModel；setModel 后 `updateUI()` 不抛且重装 MetalButtonUI。
- doClick 经模型触发一次 ActionEvent（命令=当前 actionCommand/text）。
- `setSelected(true)` 可置位并触发 1 个 ItemEvent（普通按钮无翻转语义）。
- 对齐 setter 往返；rolloverEnabled 默认 true（Metal）。

自动化实现位于同目录 `*Test.java`（headless，Metal LaF + EDT；无 @key）。

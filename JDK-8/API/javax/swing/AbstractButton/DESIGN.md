# javax.swing.AbstractButton 测试设计

## 1. API 功能
所有按钮类（JButton/JToggleButton/JCheckBox/JRadioButton）的抽象基类：文本/图标/边距、
actionCommand、mnemonic 与 displayedMnemonicIndex、四组对齐属性（文字对齐 + 文字位置）、
doClick、setAction 双向同步、ButtonModel 委托（armed/pressed/selected 状态机）、
ButtonGroup 互斥。

## 2. Java 用户如何使用
`button.setActionCommand("save"); button.addActionListener(...); button.doClick();`
或 `button.setAction(saveAction)` 由 Action 驱动；互斥选择把若干 toggle/radio 加进一个
`ButtonGroup`，靠 `getSelection/clearSelection` 管理组状态。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/AbstractButton.java`；`test/jdk/javax/swing/AbstractButton/`；
MetalLookAndFeel 的按钮边距/边框默认值来自 `MetalBorders`/`Button.margin`。

## 4. 测试用例设计 + 易错细节
- Metal 默认值锁定：horizontal/verticalAlignment=CENTER、horizontalTextPosition=TRAILING(11)、
  verticalTextPosition=CENTER、iconTextGap=4、margin=InsetsUIResource[2,14,2,14]、
  rolloverEnabled=true、borderPainted=true、focusPainted=true、multiClickThreshhold=0、
  displayedMnemonicIndex=-1。
- `setText(null)` → `getText()==null`（**不是** ""）；隐式 actionCommand 跟随当前 text，
  显式 setActionCommand 后不跟随；`setActionCommand(null)` 恢复隐式语义
  （doClick 事件里的命令回到当前 text）。
- doClick()/doClick(ms) 都触发 ActionEvent，`getWhen()>0`。
- 4 个对齐 setter 传 99 全部抛 IllegalArgumentException（类型断言，不断言 message）。
- **非对称反例**：`setIconTextGap(-1)` 在按钮上不抛（gap 原样 -1）。
- setMnemonic 触发属性名 `"mnemonic"`；`setDisplayedMnemonicIndex(99)` 抛 IAE。
- 6 个 icon setter 都是纯身份存储；**disabledIcon 派生只对 ImageIcon 生效**
  （LaF 用 GrayFilter 生成灰度副本，查询时才派生，与 enabled 无关）；
  自定义 Icon 实现拿不到派生图标；显式 setDisabledIcon 优先。
- ButtonModel 状态机：armed+pressed 时 selected 仍 false；**release（setPressed(false)）时
  ToggleButtonModel 翻转 selected，DefaultButtonModel 不翻转**（两模型对照断言）。
- `JToggleButton.addItemListener` 收到的 ItemEvent `getItem()==按钮本身`（JToggleButton
  重发事件），`getItemSelectable()==按钮`。
- setModel：换模型不触发 ChangeEvent；handler 监听器迁移到新模型（新模型 ActionListener 计数 1）；
  组件侧监听器保持。
- ButtonGroup：`setSelected(true)` 在组内**切换**选择（先前的被取消）；remove 掉选中成员后
  `getSelection()==null` 且该成员保留自身状态、行为独立；`clearSelection()` 全清。
- setAction：同步 text/tooltip/mnemonic/enabled；之后 Action 改 NAME/SHORT_DESCRIPTION
  仍持续同步；doClick 转发到 Action.actionPerformed；`setAction(null)` 清 text、恢复 enabled；
  `setHideActionText(true)` 抑制 text 同步、false 恢复。

自动化实现位于同目录 `*Test.java`（headless，Metal LaF + EDT）。

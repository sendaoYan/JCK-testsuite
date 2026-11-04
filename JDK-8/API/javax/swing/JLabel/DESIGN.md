# javax.swing.JLabel 测试设计

## 1. API 功能
不可编辑标签：文本+图标+iconTextGap 组合显示、四组对齐属性、labelFor 无障碍关联、
displayedMnemonic/displayedMnemonicIndex、LabelUI 委托、派生 disabledIcon。

## 2. Java 用户如何使用
`JLabel l = new JLabel("User:", SwingConstants.RIGHT); l.setLabelFor(nameField);`
表单标签配 mnemonic：`l.setDisplayedMnemonic('U')` 让 Alt+U 聚焦输入框。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/JLabel.java`；`test/jdk/javax/swing/JLabel/`；
SwingUtilities.layoutCompoundLabel 的布局参数来自这些属性。

## 4. 测试用例设计 + 易错细节
- 构造器矩阵；no-arg 的 **text 是 ""**（LaF 默认，不是 null），但
  `setText(null)` → `getText()==null`（两种"空"要分开断言）。
- Metal 默认：horizontalAlignment=LEADING(10)、verticalAlignment=CENTER、
  horizontalTextPosition=TRAILING、verticalTextPosition=CENTER、iconTextGap=4、
  displayedMnemonic=0、displayedMnemonicIndex=-1、autoscrolls=false；
  `LabelUI`/`MetalLabelUI`。
- 4 个对齐 setter 传 99 抛 IllegalArgumentException；**iconTextGap 无校验**
  （-1 原样接受——按钮侧同样如此，两侧一致）。
- setDisplayedMnemonic('B') 后 **UI 自动派生 displayedMnemonicIndex**（"abc"→1）；
  setDisplayedMnemonicIndex 只做范围校验：越界/负数/null 文本抛 IAE，
  **不要求 index 处字符与 mnemonic 匹配**（0 也合法）；失败调用后索引保持不变。
- disabledIcon 派生只对 ImageIcon 生效（GrayFilter 灰度副本，查询时派生）；
  自定义 Icon 无派生；显式 setDisabledIcon 优先。

自动化实现位于同目录 `*Test.java`（headless，Metal LaF + EDT）。

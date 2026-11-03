# javax.swing.JLabel — DESIGN

## 1. API 功能
不可编辑文本/图标标签（`extends JComponent`，implements `SwingConstants/Accessible`）。核心方法分组：
- 构造器：`JLabel()`、`JLabel(String)`、`JLabel(Icon)`、`JLabel(String, Icon, int)`、`JLabel(String, int)`。
- 文本/图标：`setText/getText`、`setIcon/getIcon`、`setDisabledIcon/getDisabledIcon`、`setIconTextGap/getIconTextGap`。
- 对齐：`setHorizontalAlignment/getHorizontalAlignment`、`setVerticalAlignment/getVerticalAlignment`（非法值 IAE）。
- 文本位置：`setHorizontalTextPosition/getHorizontalTextPosition`、`setVerticalTextPosition/getVerticalTextPosition`。
- 助记符：`setDisplayedMnemonic(char|int)/getDisplayedMnemonic`、`setDisplayedMnemonicIndex/getDisplayedMnemonicIndex`（非法 index IAE）。
- 关联控件：`setLabelFor/getLabelFor`。

## 2. Java 用户如何使用
`new JLabel("Hello")` → `setIcon(icon)` → `setIconTextGap(12)` → `setHorizontalAlignment(CENTER)` → `setLabelFor(textField)`（点击标签聚焦目标控件）→ `setDisplayedMnemonic('l')` 高亮匹配字符。属性变更触发 `PropertyChangeEvent`（"text"、"labelFor"、"mnemonic" 等）。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicOptionPaneUI.java`：`new JLabel(...)` 显示消息文本 + icon。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JLabel/`：iconTextGap、displayedMnemonic 自动索引、对齐往返断言。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JLabel.java`：`setDisplayedMnemonic(char)` 自动定位字符索引（找不到则 -1）。

## 4. 测试用例设计（黑盒部分，`JLabelTest.testBlackBox`）
1. 构造文本往返 + icon/disabledIcon 身份 + iconTextGap 默认 4 与往返。
2. setLabelFor 往返 + "labelFor"/"text" PropertyChangeEvent。
3. horizontal/verticalAlignment 常量往返 + 非法值 IAE；vertical/horizontalTextPosition 往返 + 非法 IAE。
4. displayedMnemonic 链：char 存大写 keycode、setText 后 setDisplayedMnemonic('l') 自动索引 2、int 往返、手动 index、非法 index IAE、空文本 index 0 IAE。
5. 装入 JFrame 后 displayable/visible/showing 断言。

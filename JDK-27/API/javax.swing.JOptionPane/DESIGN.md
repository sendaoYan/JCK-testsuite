# javax.swing.JOptionPane 测试设计

## 1. API 功能
- 静态常量：`DEFAULT_OPTION/YES_NO_OPTION/…`、`ERROR_MESSAGE/INFORMATION_MESSAGE/…/PLAIN_MESSAGE`、`YES_OPTION/NO_OPTION/CANCEL_OPTION/OK_OPTION/CLOSED_OPTION`、`UNINITIALIZED_VALUE` 及 `*_PROPERTY` 属性名。
- 构造器：`JOptionPane()`（默认消息 "JOptionPane message"）、`JOptionPane(Object[, int[, int[, Icon[, Object[]]]]])`。
- 属性往返：`setMessage/getMessage`、`setMessageType/getMessageType`、`setOptionType/getOptionType`、`setIcon/getIcon`、`setOptions/getOptions`（防御性拷贝）、`setInitialValue/getInitialValue`、`setValue/getValue`、`setSelectionValues/getSelectionValues`、`setInitialSelectionValue`、`setInputValue/getInputValue`、`setWantsInput/getWantsInput`。
- 静态辅助：`getRootFrame()`、`getFrameForComponent(Component)`、`getDesktopPaneForComponent(Component)`。
- 对话框工厂（不显示）：`createDialog(String)`、`createDialog(Component,String)`。
- 注意：`showXxxDialog/showConfirmDialog` 是模态阻塞，测试中**禁止调用**。

## 2. Java 用户如何使用
典型组合：`new JOptionPane("msg", WARNING_MESSAGE, YES_NO_OPTION)` → 设置属性 → `createDialog(parent, title)` 拿到 `JDialog` 再 `setVisible(true)`；`showMessageDialog` 之外的底层用法通过 `getRootFrame()`/`getFrameForComponent()` 获取宿主窗口。

## 3. 开源代码实际用法调研
- JDK 自带测试 `test/jdk/javax/swing/plaf/synth/Test6660049.java` 与 `plaf/metal/OceanTheme/4969419/bug4969419.java`：实例化 JOptionPane 并检查其组件/属性，用于 L&F 回归。
- JDK 源码 `javax.swing.JOptionPane.showInputDialog/showMessageDialog`：内部 `new JOptionPane(...)` + `createDialog` + `setVisible(true)`，证明 `createDialog` 是非模态工厂的核心链路。
- 常见应用用 `JOptionPane.showMessageDialog` 弹提示，或用 `createDialog` 自定义按钮后自行显示。

## 4. 测试用例设计 + 自动化实现
黑盒覆盖：全部静态常量精确值；默认构造器属性；带 messageType/optionType 构造器；message/messageType/optionType/icon/options/initialValue/value/selectionValues/initialSelectionValue/inputValue/wantsInput 往返；`getMaxCharactersPerLineCount()==Integer.MAX_VALUE`；`getRootFrame` 非 null；`getFrameForComponent` 回退 root frame；`getDesktopPaneForComponent(null)==null` 及对 JDesktopPane 命中；`createDialog(String)` 与 `createDialog(Component,String)` 返回未显示的 JDialog 并 dispose。见 `JOptionPaneTest.java` 的 `testBlackBox()`。

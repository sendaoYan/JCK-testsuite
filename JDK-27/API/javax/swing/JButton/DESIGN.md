# javax.swing.JButton — DESIGN

## 1. API 功能
普通按钮（`extends javax.swing.AbstractButton`，implements `Accessible`）。核心方法分组：
- 构造器：`JButton()`、`JButton(String)`、`JButton(Icon)`、`JButton(Action)`。
- 文本/图标：`setText/getText`、`setIcon/getIcon`、`setActionCommand/getActionCommand`、`setMnemonic(char|int)/getMnemonic`。
- Action 绑定：`setAction(Action)/getAction()`（text/enabled 随 Action 属性同步）。
- 点击：`doClick()`（触发 ActionListener + 模型 ChangeEvent）。
- 模型：`setModel/getModel`、`setRolloverEnabled/isRolloverEnabled`、`setDefaultCapable/isDefaultCapable/isDefaultButton`。
- 根窗格默认按钮：`JRootPane.setDefaultButton/getDefaultButton`。

## 2. Java 用户如何使用
`new JButton("Press")` → `setMnemonic('P')` → `setActionCommand("fire")` → `addActionListener(e -> ...)` → `doClick()` 触发回调；或 `button.setAction(abstractAction)` 让文本/启用态跟随 Action（`Action.NAME` 变化同步到 text）。作为默认按钮：`frame.getRootPane().setDefaultButton(button)`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicOptionPaneUI.java`：`new JButton(...)` + `addActionListener` 构造对话框按钮。
- `~/git/jdk25u-dev/test/jdk/javax/swing/JButton/` 用例：`doClick` 驱动 ActionEvent、`setDefaultCapable` 影响根窗格默认按钮。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/AbstractButton.java`：`doClick()` 经模型 press/release 触发 `fireActionPerformed`。

## 4. 测试用例设计（黑盒部分，`JButtonTest.testBlackBox`）
1. 默认文本 ""、模型非 null；setText/setMnemonic(char|int)/setActionCommand 往返；`new JButton(Icon)` 图标身份。
2. setAction 绑定：getAction 同实例、text 同步 NAME、enabled 同步、Action 禁用/启用反向同步、NAME 变更更新 text。
3. doClick：一次 ActionEvent、command 跟随 NAME、模型 ChangeEvent 计数（armed/pressed 4 次）、普通按钮无 ItemEvent；禁用 doClick 无事件。
4. setModel 往返 + 共享 DefaultButtonModel 无组 + actionCommand 委托模型。
5. rollover/defaultCapable 往返；根窗格 setDefaultButton/isDefaultButton 链（装入 JFrame 后）。

## 5. 组件层深化批（`testContract`，Metal LaF）
- UI 身份：`getUIClassID()=="ButtonUI"`；Metal 下 `MetalButtonUI`；
  `UIManager.setLookAndFeel` 重设后已有按钮 UI 更新。
- Metal 默认：iconTextGap=4、margin=InsetsUIResource[2,14,2,14]、multiClickThreshhold=0
  （setMultiClickThreshhold 往返）；按钮侧 `setIconTextGap(-1)` 不抛。
- 4 个对齐 setter 传 99 抛 IllegalArgumentException。
- actionCommand 回退链：显式覆盖 → `setActionCommand(null)` → doClick 事件命令回到
  当前 text；`doClick(ms)` 变体同样触发。
- **disabledIcon 派生只对 ImageIcon 生效**（GrayFilter 灰度副本、查询时派生、
  保留源尺寸）；自定义 Icon 无派生；显式 setDisabledIcon 优先。
- 换模型后 updateUI 不抛并重装 MetalButtonUI。

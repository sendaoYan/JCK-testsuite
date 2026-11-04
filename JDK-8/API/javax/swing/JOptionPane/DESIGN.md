# javax.swing.JOptionPane 测试设计（JDK-8 侧）

## 1. API 功能
标准对话框组件：message/messageType/optionType/value/wantsInput/options/selectionValues 属性族；
常量族（消息类型 0..3/-1、选项类型 -1..2、返回值 0/1/2/-1、UNINITIALIZED_VALUE）；headless 下
getFrameForComponent/createDialog/show* 抛 HeadlessException。

## 2. Java 用户如何使用
`new JOptionPane("Save?", QUESTION_MESSAGE, YES_NO_OPTION)`；`pane.setValue(NO_OPTION)`；
`pane.createDialog(parent, "t")`（headless 环境抛 HeadlessException）。

## 3. 开源代码实际用法调研（本地源码+自带测试，web 未使用）
`javax/swing/JOptionPane.java`；JDK 测试 `test/javax/swing/JOptionPane/`；本仓库 probe。

## 4. 测试用例设计 + 易错细节（headless 锁定）
- 常量矩阵全量锁定（含 UNINITIALIZED_VALUE=="uninitializedValue" 字符串、属性名常量）。
- 默认值：messageType==-1(PLAIN)、optionType==-1(DEFAULT)、value 同一 UNINITIALIZED_VALUE 静态实例、
  options/initialValue/initialSelectionValue/selectionValues/icon 全 null、
  maxCharactersPerLineCount==Integer.MAX_VALUE。
- **非法类型：setMessageType(99)/setOptionType(99)/构造器传 99 抛 RuntimeException（不是
  IllegalArgumentException）**，且抛出后原值不变（校验在赋值前）。
- null 语义：setValue(null)/setOptions(null)/setSelectionValues(null) 均 → null。
- 事件名：message/messageType/optionType/wantsInput/value。
- 6 参构造器 (message,type,optionType,icon,options,initialValue) 全字段保留（不存在 7 参构造器）。
- headless：createDialog(两种重载)/getFrameForComponent(null|panel)/showInputDialog → HeadlessException。
- getUIClassID "OptionPaneUI"；Metal 下 BasicOptionPaneUI。

自动化实现位于同目录 `JOptionPaneTest.java`（headless，无 @key）。

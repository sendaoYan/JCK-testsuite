# javax.security.auth.callback.ConfirmationCallback 设计案

## 1. API 功能
- 四个构造器：`(int messageType, int optionType, int defaultOption)`、`(int messageType, String[] options, int defaultOption)`、`(String prompt, int messageType, int optionType, int defaultOption)`、`(String prompt, int messageType, String[] options, int defaultOption)`。options 数组会被克隆；非法输入抛 `IllegalArgumentException`。
- 查询：`getPrompt()`（无 prompt 构造时为 null）、`getMessageType()`、`getOptionType()`（自定义 options 时为 `UNSPECIFIED_OPTION`）、`getOptions()`（返回副本，预设选项时为 null）、`getDefaultOption()`、`getSelectedIndex()`。
- 设置选择：`setSelectedIndex(int)` / `getSelectedIndex()`。
- 常量：响应值 `YES=0,NO=1,CANCEL=2,OK=3`；选项类型 `YES_NO_OPTION=0,YES_NO_CANCEL_OPTION=1,OK_CANCEL_OPTION=2,UNSPECIFIED_OPTION=-1`；消息类型 `INFORMATION=0,WARNING=1,ERROR=2`。
- 实现 `Callback, java.io.Serializable`。

## 2. Java 用户如何使用
- 安全服务构造回调交给 handler；handler 按 `getOptionType()`（预设）或 `getOptions()`（自定义）展示选项，用户选择后 `setSelectedIndex(...)`，服务端 `getSelectedIndex()` 读取。
- 典型链路：`new ConfirmationCallback("Continue?", INFORMATION, YES_NO_OPTION, NO)` → handler 展示 → `setSelectedIndex(YES)` → 服务 `getSelectedIndex()==YES`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`（JDK-27 实现）。
- `src/java.base/share/classes/sun/security/util/ConsoleCallbackHandler.java` 的 `doConfirmation`：`switch (optionType) { case YES_NO_OPTION: options = new OptionInfo[]{new OptionInfo("Yes", YES), new OptionInfo("No", NO)}; break; ... case UNSPECIFIED_OPTION: String[] optionStrings = confirmation.getOptions(); ... }`，是 `getOptionType()`/`getOptions()`/常量组合的真实用法。
- `test/jdk/javax/security/auth/callback/Mutability.java`（bug 8242330）：`String[] i11={"1","2"}; ConfirmationCallback c1 = new ConfirmationCallback(INFORMATION, i11, 0); i11[0]="x"; assertEQ(c1.getOptions()[0], "1");`，验证构造器克隆 options 与 `getOptions()` 返回副本。

## 4. 测试用例设计 + 自动化实现
- 结构（`testWhiteBox`）：public 类、实现 Callback + Serializable、extends Object、含全部 7 个方法与 11 个常量字段。
- 行为（`testBlackBox`）：常量值逐一断言；四个构造器分别验证 prompt 有无、optionType（预设/UNSPECIFIED）、options（预设 null/自定义副本）、defaultOption；构造器克隆 options、`getOptions()` 返回副本；`setSelectedIndex`/`getSelectedIndex` 往返；非法 messageType/optionType/defaultOption/options/元素/prompt 全部抛 `IllegalArgumentException`。

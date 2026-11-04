# javax.security.auth.callback.ChoiceCallback 设计案

## 1. API 功能
- 构造器 `ChoiceCallback(String prompt, String[] choices, int defaultChoice, boolean multipleSelectionsAllowed)`：构造一个「从选项列表中选择」的回调。choices 数组会被克隆；非法输入（prompt 空/空串、choices 空/空数组、含空元素、defaultChoice 越界）抛 `IllegalArgumentException`。
- 查询：`getPrompt()`、`getChoices()`（返回副本）、`getDefaultChoice()`、`allowMultipleSelections()`、`getSelectedIndexes()`（返回副本，未设置时为 null）。
- 设置选择：`setSelectedIndex(int)`（单选，存入长度 1 数组）、`setSelectedIndexes(int[])`（多选，输入被克隆；当 `multipleSelectionsAllowed == false` 时抛 `UnsupportedOperationException`）。
- 实现 `Callback, java.io.Serializable`。

## 2. Java 用户如何使用
- 安全服务构造回调交给 handler；handler 调用 `getChoices()` 展示选项，根据 `allowMultipleSelections()` 决定单/多选，随后 `setSelectedIndex(i)` 或 `setSelectedIndexes(new int[]{...})`，服务端再 `getSelectedIndexes()` 读取。
- 典型链路：`new ChoiceCallback("Realm", new String[]{"r1","r2"}, 0, true)` → handler 展示 → `setSelectedIndexes(new int[]{0,1})` → 服务 `getSelectedIndexes()` 得到 `[0,1]`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`（JDK-27 实现）。
- `test/jdk/javax/security/auth/callback/Mutability.java`（bug 8242330）：验证 `ChoiceCallback` 构造器克隆 choices、`getChoices()` 返回副本、`setSelectedIndexes` 克隆入参、`getSelectedIndexes` 返回副本。片段：`String[] i31 = {"1","2"}; ChoiceCallback c3 = new ChoiceCallback("Hi", i31, 0, true); i31[0]="x"; assertEQ(c3.getChoices()[0], "1");`。
- `src/java.naming/share/classes/com/sun/jndi/ldap/sasl/DefaultCallbackHandler.java` 对 `RealmChoiceCallback`（ChoiceCallback 子类）使用 `getChoices()` + `setSelectedIndex(selected)` 的相同模式。

## 4. 测试用例设计 + 自动化实现
- 结构（`testWhiteBox`）：public 类、实现 Callback + Serializable、extends Object、含全部 7 个方法。
- 行为（`testBlackBox`）：构造与 getter 往返；构造器克隆 choices 与 getChoices 返回副本（互不影响）；`getSelectedIndexes` 未设置返回 null；单选 `setSelectedIndex`；多选 `setSelectedIndexes` 的入参/返回值克隆；`allowMultipleSelections=false` 时 `setSelectedIndex` 可用、`setSelectedIndexes` 抛 `UnsupportedOperationException`；`setSelectedIndexes(null)` 与空数组边界；构造器各非法输入（prompt/choices/元素/defaultChoice）均抛 `IllegalArgumentException`。

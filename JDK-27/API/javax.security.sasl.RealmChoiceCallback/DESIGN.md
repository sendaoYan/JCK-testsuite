# javax.security.sasl.RealmChoiceCallback 测试设计

## 1. API 功能
SASL 领域多选回调，继承 `javax.security.auth.callback.ChoiceCallback`：
- 构造：`RealmChoiceCallback(String prompt, String[] choices, int defaultChoice, boolean allowMultiple)`。
- 继承方法：`getPrompt()`、`getChoices()`（返回克隆）、`getDefaultChoice()`、`allowMultipleSelections()`、`setSelectedIndex(int)`、`setSelectedIndexes(int[])`（多选禁用时抛 `UnsupportedOperationException`）、`getSelectedIndexes()`（未选择返回 null）。

## 2. Java 用户如何使用
SASL 客户端在多个可选 realm 中选择：
```java
if (cb instanceof RealmChoiceCallback) {
    RealmChoiceCallback rcb = (RealmChoiceCallback) cb;
    String[] choices = rcb.getChoices();
    int selection = userPicks(choices);
    if (selection < 0) selection = rcb.getDefaultChoice();
    rcb.setSelectedIndex(selection);
}
```
典型链路：`new RealmChoiceCallback(...) → getChoices/getDefaultChoice/allowMultipleSelections → setSelectedIndex(es) → getSelectedIndexes`。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/test/jdk/com/sun/security/sasl/ClientCallbackHandler.java`（第 65–90 行）：遍历 `rcb.getChoices()`，空输入取 `rcb.getDefaultChoice()`，否则 `Integer.parseInt` 后 `rcb.setSelectedIndex(selection)`。
- 构造/异常约束依据源码 `src/java.base/share/classes/javax/security/auth/callback/ChoiceCallback.java`：choices 克隆、非法参数抛 `IllegalArgumentException`、`setSelectedIndexes` 在多选禁用时抛 `UnsupportedOperationException`、`getChoices/getSelectedIndexes` 返回克隆。

## 4. 测试用例设计 + 自动化实现
黑盒（`RealmChoiceCallbackTest.testBlackBox()`）：构造取 prompt/choices 内容/defaultChoice/allowMultiple；`getChoices` 防御性拷贝；单选中 `setSelectedIndex/getSelectedIndexes`、`setSelectedIndexes` 抛 `UnsupportedOperationException`；多选中 `setSelectedIndexes/getSelectedIndexes`；异常路径：负索引、越界索引、null choices、空 choices 均抛 `IllegalArgumentException`。
白盒（`testWhiteBox()`）：public、具体类、父类 `ChoiceCallback`、可赋值给 `Callback`/`Serializable`、拥有 `(String,String[],int,boolean)` 公共构造。

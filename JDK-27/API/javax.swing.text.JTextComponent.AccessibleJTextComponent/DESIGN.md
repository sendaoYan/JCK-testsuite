# javax.swing.text.JTextComponent.AccessibleJTextComponent — DESIGN

## 1. API 功能
`JTextComponent` 的公开内部类（`public class AccessibleJTextComponent extends JComponent.AccessibleJComponent`），实现无障碍接口 `AccessibleText/CaretListener/DocumentListener/AccessibleAction/AccessibleEditableText/AccessibleExtendedText`，由 `JTextComponent.getAccessibleContext()` 返回。
- 文本访问：`getAccessibleText()/getCharCount()/getCaretPosition()/getSelectionStart()/getSelectionEnd()/getSelectedText()/getTextRange(int,int)/getAtIndex(...)/getCharacterAttribute(int)`。
- 编辑：`getAccessibleEditableText()/setTextContents(String)`。
- 动作：`getAccessibleAction()/getAccessibleActionCount()/getAccessibleActionDescription(int)/doAccessibleAction(int)`。

## 2. Java 用户如何使用
`JTextArea.getAccessibleContext()` 得到 `AccessibleContext`，向下转型为 `AccessibleJTextComponent`，读 `getAccessibleText()`/`getCharCount()`/`getCaretPosition()`/选区信息，或 `setTextContents(...)` 改文本；`getAccessibleText()/getAccessibleEditableText()/getAccessibleAction()` 均返回 `this`。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/JTextComponent.java`（`getAccessibleContext`）：`if (accessibleContext == null) accessibleContext = new AccessibleJTextComponent();`。
- 同文件 `getAccessibleText`：`return this;`；`getAccessibleRole`：`return AccessibleRole.TEXT;`；`getAccessibleStateSet`：editable 时 `states.add(AccessibleState.EDITABLE)`。
- 同文件 `getCaretPosition/getSelectionStart/getSelectionEnd/getSelectedText`：直接委托 `JTextComponent.this.*`。
- 同文件 `setTextContents`：`JTextComponent.this.setText(s);`；`getIndexAtPoint(null)` 返回 -1。

## 4. 测试用例设计（黑盒部分，`AccessibleJTextComponentTest.testBlackBox`）
1. getAccessibleContext 类型 + getAccessibleText/getAccessibleEditableText/getAccessibleAction 返回 this。
2. getAccessibleRole == TEXT；editable 状态存在。
3. getCharCount、getIndexAtPoint(null)==-1、getCharacterAttribute(0) 非空。
4. caret/选区镜像：setCaretPosition/select 后 getCaretPosition/getSelectionStart/End/getSelectedText 一致。
5. getTextRange(0,5)；setTextContents 更新组件文本与 charCount。
6. getAccessibleActionCount>0、getAccessibleActionDescription(0) 非空、越界(-1) 返回 null。

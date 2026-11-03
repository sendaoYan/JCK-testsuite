# javax.swing.text.DefaultEditorKit.BeepAction — DESIGN

## 1. API 功能
`DefaultEditorKit` 的公开静态嵌套类（`public static class BeepAction extends TextAction`），用于在文本组件上给出"错误反馈"提示音。
- 构造器：`BeepAction()`（无参，内部 `super(beepAction)`，动作名为 `DefaultEditorKit.beepAction = "beep"`）。
- 继承自 `TextAction`/`AbstractAction`：`getValue(Action.NAME)` 返回 `"beep"`。
- 核心操作：`actionPerformed(ActionEvent e)` —— 通过 `TextAction.getTextComponent(e)` 定位目标组件，调用 `UIManager.getLookAndFeel().provideErrorFeedback(target)`（最终 `Toolkit.beep()` 蜂鸣）。

## 2. Java 用户如何使用
`new DefaultEditorKit.BeepAction()` 得到动作，构造 `ActionEvent(component, ACTION_PERFORMED, DefaultEditorKit.beepAction)` 后调用 `actionPerformed(evt)`；组件为事件 source 时对其 beep，事件 source 非文本组件或事件为 null 时回退到最近获焦文本组件/默认 Toolkit beep。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultEditorKit.java`（`BeepAction.actionPerformed`）：`JTextComponent target = getTextComponent(e); if (target != null) UIManager.getLookAndFeel().provideErrorFeedback(target);`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/TextAction.java`（`getTextComponent`）：事件 source 是 `JTextComponent` 则取 source，否则 `getFocusedComponent()`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/LookAndFeel.java`（`provideErrorFeedback`）：component 非 null 用 `component.getToolkit()`，否则 `Toolkit.getDefaultToolkit()`，然后 `toolkit.beep()`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/DefaultEditorKit/bug4265242.java`：`actionPerformed(new ActionEvent(jTextPane, ACTION_PERFORMED, DefaultEditorKit.endParagraphAction))` 是动作驱动的惯用写法。

## 4. 测试用例设计（黑盒部分，`BeepActionTest.testBlackBox`）
1. `new BeepAction()` 是 `Action` 实例，`getValue(Action.NAME)` 等于 `DefaultEditorKit.beepAction`（"beep"）。
2. JFrame + JTextArea 显示后，以 area 为 source 触发 `actionPerformed`（触发蜂鸣路径），断言不抛异常且文本不变。
3. 以 null 事件触发 `actionPerformed`（走 `getFocusedComponent` 回退 + 默认 Toolkit beep），断言不抛异常且文本不变。

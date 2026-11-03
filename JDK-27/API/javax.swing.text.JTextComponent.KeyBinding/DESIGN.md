# javax.swing.text.JTextComponent.KeyBinding — DESIGN

## 1. API 功能
`JTextComponent` 的公开静态嵌套类（`public static class KeyBinding`），一个键绑定记录，把 `KeyStroke` 与动作名关联，配合 `JTextComponent.loadKeymap(...)` 批量装载键映射。
- 字段：`public KeyStroke key;`、`public String actionName;`。
- 构造器：`KeyBinding(KeyStroke key, String actionName)`（直接赋值两个字段）。

## 2. Java 用户如何使用
`new KeyBinding(KeyStroke.getKeyStroke(...), "action-name")` 生成绑定，再用 `JTextComponent.loadKeymap(keymap, new KeyBinding[]{...}, new Action[]{...})` 把每个绑定按 `actionName` 匹配同名 Action 后 `addActionForKeyStroke`；之后 `keymap.getAction(ks)` 可取回绑定动作。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/JTextComponent.java`（`KeyBinding`）：`public KeyBinding(KeyStroke key, String actionName) { this.key = key; this.actionName = actionName; }`。
- 同文件 `loadKeymap`：按 `Action.NAME` 建 `Hashtable<String,Action>`，对每个 `KeyBinding` `h.get(binding.actionName)` 匹配后 `map.addActionForKeyStroke(binding.key, a)`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/text/DefaultEditorKit/bug4265242.java`：按 `getValue(Action.NAME)` 匹配动作名后触发，体现同名动作的惯例。

## 4. 测试用例设计（黑盒部分，`KeyBindingTest.testBlackBox`）
1. `new KeyBinding(KeyStroke.getKeyStroke('A'), DefaultEditorKit.cutAction)` 后 `key`/`actionName` 字段往返。
2. 用字符串 `"control B"` 构造 KeyStroke 的绑定字段往返。
3. 典型组合：`addKeymap` + `loadKeymap`（Cut/Copy 动作与绑定数组）后 `getAction(ks)` 返回对应动作、`isLocallyDefined(ks)` 为真，最后 `removeKeymap` 清理。

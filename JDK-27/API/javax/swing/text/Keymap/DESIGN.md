# javax.swing.text.Keymap — DESIGN

## 1. API 功能
键绑定接口（映射 KeyStroke→Action），支持按名称注册与父子解析链。方法：`getName()`、`getDefaultAction()/setDefaultAction()`、`getAction(KeyStroke)`、`getBoundKeyStrokes()/getBoundActions()/getKeyStrokesForAction()`、`isLocallyDefined(KeyStroke)`、`addActionForKeyStroke()/removeKeyStrokeBinding()/removeBindings()`、`getResolveParent()/setResolveParent()`。配套静态工厂在 JTextComponent：`addKeymap/getKeymap/removeKeymap/loadKeymap`。

## 2. Java 用户如何使用
`Keymap k = JTextComponent.addKeymap("name", parent)` → `k.addActionForKeyStroke(KeyStroke.getKeyStroke('A'), action)` → `k.getAction(key)` 查询；`setDefaultAction` 设默认动作；`JTextArea.getKeymap()/setKeymap()` 挂到组件。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/text/JTextComponent.java`：`getKeymapTable()` 中 `addKeymap(DEFAULT_KEYMAP, null)` 初始化默认 keymap；`loadKeymap` javadoc 给出绑定 cut/copy/paste 的完整示例；`DefaultKeymap` 实现解析链（`getAction` 本地未命中则查 parent）。
- `test/jdk/javax/swing/plaf/nimbus/TestNimbusOverride.java` 使用 keymap/loadKeymap 覆盖 Nimbus 默认绑定。

## 4. 测试用例设计（黑盒部分，`KeymapTest.testBlackBox`）
1. addKeymap/名称/默认解析父/默认动作往返。
2. addActionForKeyStroke → getAction/isLocallyDefined/getBoundKeyStrokes/getBoundActions/getKeyStrokesForAction。
3. removeKeyStrokeBinding 解除、removeBindings 清空。
4. 父子解析：child 继承 parent 绑定、isLocallyDefined 区分、getKeyStrokesForAction 继承。
5. 命名注册表 getKeymap/removeKeymap 往返；JTextArea setKeymap/getKeymap 往返；loadKeymap 绑定命名动作。

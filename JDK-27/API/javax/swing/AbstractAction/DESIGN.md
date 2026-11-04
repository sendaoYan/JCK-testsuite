# javax.swing.AbstractAction 设计案

## 1. API 功能
`AbstractAction` 是 `Action` 接口的抽象基类，封装一组键值对（`Action.NAME`、`SHORT_DESCRIPTION` 等常量键）与属性变更支持。方法分组：
- 构造：`AbstractAction()` / `AbstractAction(String)` / `AbstractAction(String, Icon)`。
- 值存取：`getValue(String)`、`putValue(String, Object)`、`getKeys()`。
- 状态：`isEnabled()`、`setEnabled(boolean)`。
- 监听：`addPropertyChangeListener`、`removePropertyChangeListener`、`getPropertyChangeListeners`。
- 事件：`actionPerformed(ActionEvent)`（默认空实现）；`clone()` 受保护。

## 2. Java 用户如何使用
用户继承 `AbstractAction` 定义可复用命令，把命令绑定到按钮/菜单/按键：
```java
Action save = new AbstractAction("Save") {
    public void actionPerformed(ActionEvent e) { /* save */ }
};
save.putValue(Action.SHORT_DESCRIPTION, "Save the file");
button.setAction(save);
```
典型链路：`new AbstractAction(name){}` → `putValue(NAME/SHORT_DESCRIPTION)` → `getValue` → `getKeys` → `setEnabled` → 绑定组件。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/text/TextAction.java`：`public abstract class TextAction extends AbstractAction`，用 `getValue`/`putValue` 存 `NAME`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/TextAction.java`）。
- JDK 源码 `javax/swing/DefaultEditorKit.java`：大量静态 `TextAction` 子类（如 `DefaultEditorKit$InsertContentAction extends TextAction`）继承 `AbstractAction`。
- JDK 源码 `javax/swing/plaf/basic/BasicLookAndFeel.java` 用 `AbstractAction` 实现系统按键绑定。
- JDK 测试 `test/jdk/javax/swing/` 多处用 `AbstractAction` 作为按钮 `Action` 匿名子类。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract、实现 `Action`+`Cloneable`+`Serializable`、父类 `Object`。
- 行为（黑盒）：`testBlackBox()` 覆盖 `Action` 常量字符串、构造器设置 NAME、`putValue/getValue` 往返、`getKeys` 包含键、`setEnabled` 状态切换、`add/remove/getPropertyChangeListeners` + `putValue` 触发 `PropertyChangeEvent`、`actionPerformed` 空实现 smoke。

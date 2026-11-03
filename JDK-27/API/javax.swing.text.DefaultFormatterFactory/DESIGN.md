# javax.swing.text.DefaultFormatterFactory — DESIGN

## 1. API 功能
`DefaultFormatterFactory extends JFormattedTextField.AbstractFormatterFactory implements Serializable`，按字段状态选择 default/display/edit/null 四种 `AbstractFormatter` 之一（`@since 1.4`）。方法分组：
- 构造器：`()`、`(defaultFormat)`、`(default, display)`、`(default, display, edit)`、`(default, display, edit, nullFormat)`。
- 存取：`setDefaultFormatter/getDefaultFormatter`、`setDisplayFormatter/getDisplayFormatter`、`setEditFormatter/getEditFormatter`、`setNullFormatter/getNullFormatter`。
- 选择逻辑 `getFormatter(JFormattedTextField)`：source 为 null→null；value 为 null→null 格式化器；否则有焦点→edit、无焦点→display；仍为 null→default。

## 2. Java 用户如何使用
`new DefaultFormatterFactory(display, display, edit)` → 安装到 `JFormattedTextField`；字段有焦点时用 edit 格式化器、失焦显示时用 display 格式化器；也可用 setter 单独配置 default/display/edit/null，再由 `getFormatter(ftf)` 取回实际生效的格式化器。

## 3. 开源代码实际用法调研（来源：JDK 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/swing/JFormattedTextField.java`（getDefaultFormatterFactory）：按类型 `new DefaultFormatterFactory(new DateFormatter(...))` / `new NumberFormatter(...)`；对 Number 类型 `new DefaultFormatterFactory(displayFormatter, displayFormatter, editFormatter)`。
- `src/java.desktop/share/classes/javax/swing/text/DefaultFormatterFactory.java` 类注释：`new DefaultFormatterFactory(displayFormatter, displayFormatter, editFormatter); JFormattedTextField tf = new JFormattedTextField(factory);`。
- `test/jdk/javax/swing/JFormattedTextField/bug4886538.java`：`field.setFormatter(((DefaultFormatterFactory) field.getFormatterFactory()).getDefaultFormatter())` 取出 default 格式化器。

## 4. 测试用例设计（黑盒部分，`DefaultFormatterFactoryTest.testBlackBox`）
1. 1/2/3/4 参数构造器逐级填充 default/display/edit/null，后序槽位为 null。
2. 无参构造器四槽位全 null；四个 setter 各自往返。
3. `getFormatter(null)` 返回 null。
4. 值 null 的 `JFormattedTextField` → null 格式化器；设非 null 值（无焦点，headless）→ display 格式化器；display 为 null 时回退到 default。

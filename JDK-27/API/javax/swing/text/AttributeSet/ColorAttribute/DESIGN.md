# javax.swing.text.AttributeSet.ColorAttribute — DESIGN

## 1. API 功能
`AttributeSet` 的嵌套空标记接口，用于给“颜色呈现”相关的属性键打类型标签。无任何方法。

## 2. Java 用户如何使用
用户以 `instanceof AttributeSet.ColorAttribute` 判断属性键是否影响颜色呈现：
```java
if (key instanceof AttributeSet.ColorAttribute) { /* 颜色样式 */ }
```

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AttributeSet.java`：`public interface ColorAttribute {}`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleConstants.java`：`Foreground = new ColorConstants("foreground")`、`Background = new ColorConstants("background")`；`ColorConstants` 同时 `implements ColorAttribute, CharacterAttribute`。

## 4. 测试用例设计 + 自动化实现（`ColorAttributeTest.testBlackBox`）
1. 接口无方法、无字段（空标记）。
2. `StyleConstants$ColorConstants` 同时实现 `ColorAttribute` 与 `CharacterAttribute`。
3. `Foreground/Background` 是 ColorAttribute 实例（且是 CharacterAttribute 实例）；`Bold` 不是 ColorAttribute。
4. `SimpleAttributeSet` 不实现该标记。
5. 行为闭环：`StyleConstants.setForeground(s, Color.red)` 后 `getForeground` 读回 `Color.red`。

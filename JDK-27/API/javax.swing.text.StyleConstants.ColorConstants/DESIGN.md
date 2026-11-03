# javax.swing.text.StyleConstants.ColorConstants — DESIGN

## 1. API 功能
`StyleConstants` 的嵌套 final 类，作为“颜色”属性键对象的运行时类型（`extends StyleConstants implements AttributeSet.ColorAttribute, AttributeSet.CharacterAttribute`）。无 public 构造器。

## 2. Java 用户如何使用
```java
StyleConstants.Foreground   // ColorConstants 实例，作为键
StyleConstants.setForeground(s, Color.red);
Color c = StyleConstants.getForeground(s);
```

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/StyleConstants.java`：`Foreground = new ColorConstants("foreground")`、`Background = new ColorConstants("background")`；`ColorConstants` 声明 `implements AttributeSet.ColorAttribute, AttributeSet.CharacterAttribute`，构造器 `private ColorConstants(String)`。

## 4. 测试用例设计 + 自动化实现（`ColorConstantsTest.testBlackBox`）
1. 无 public 构造器；同时实现 ColorAttribute 与 CharacterAttribute。
2. `Foreground/Background` 是该类实例；`Bold` 不是。
3. 行为闭环：`setForeground(Color.red)/setBackground(Color.blue)` 后读回正确颜色。

# javax.swing.text.Position.Bias — DESIGN

## 1. API 功能
`Position` 的嵌套 final 类，类型安全枚举，表示位置偏向某一侧字符。仅两个实例常量：
- `Forward`：偏向模型中的下一个字符。
- `Backward`：偏向前一个字符。
- 方法：`toString()` 返回 "Forward"/"Backward"。

## 2. Java 用户如何使用
```java
Position.Bias b = Position.Bias.Forward;
String s = b.toString();   // "Forward"
```
构造函数为 private，只能通过两个常量获取实例。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/Position.java`：`Forward = new Bias("Forward")`、`Backward = new Bias("Backward")`；`toString()` 返回构造传入的 name；构造器 `private Bias(String name)`。

## 4. 测试用例设计 + 自动化实现
`BiasTest`（9 断言）：
- 仅 Forward/Backward 两个 public static final 常量实例，非 null 且互异。
- toString 精确为 "Forward"/"Backward"。
- getDeclaredFields 无其他公有实例字段（枚举化封闭集合）。

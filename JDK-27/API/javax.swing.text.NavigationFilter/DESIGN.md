# javax.swing.text.NavigationFilter — DESIGN

## 1. API 功能
光标定位过滤器，可限制光标（dot）可放置的位置。当 `JTextComponent` 设置了非 null 的 NavigationFilter 时，DefaultCaret 在改变选择时会回调过滤器。方法分组：
- 构造器：`NavigationFilter()`。
- 过滤钩子：`setDot(FilterBypass,int,Position.Bias)`、`moveDot(FilterBypass,int,Position.Bias)`（默认直接调用旁路放行）。
- 视觉导航：`getNextVisualPositionFrom(JTextComponent,int,Bias,int,Bias[])`（默认转发给 UI；非法 direction 抛 `IllegalArgumentException`，非法位置抛 `BadLocationException`）。

## 2. Java 用户如何使用
`area.setNavigationFilter(new NavigationFilter(){ 覆写 setDot/moveDot 做钳制 })` → `area.setCaretPosition(n)` / `moveCaretPosition(m)` 会先进入过滤器，过滤器内可 `fb.setDot(clamped, bias)` 改变目标位置或直接 `fb.setDot(dot, bias)` 放行。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultFormatter.java`（`DefaultNavigationFilter`）：`public void setDot(FilterBypass fb, int dot, Position.Bias bias) { if (tc.composedTextExists()) { fb.setDot(dot, bias); } else { DefaultFormatter.this.setDot(fb, dot, bias); } }` —— 输入法组合态放行、否则转发给格式化器。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultFormatter.java`：`getNextVisualPositionFrom` 中 `text.getUI().getNextVisualPositionFrom(text, pos, bias, direction, biasRet)` —— 默认转发给 UI。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/DefaultCaret.java`（`setDot/moveDot`）：`NavigationFilter filter = component.getNavigationFilter(); if (filter != null) { filter.setDot(getFilterBypass(), dot, dotBias); }` —— Caret 位置变更会经过过滤器。

## 4. 测试用例设计（黑盒部分，`NavigationFilterTest.testBlackBox`）
1. 默认过滤器 setDot/moveDot 放行：setCaretPosition/moveCaretPosition 正常。
2. 自定义过滤器钳制 setDot 到 3、moveDot 到文档长度；FilterBypass.getCaret() 返回组件 Caret。
3. FilterBypass 直接 setDot/moveDot 绕过过滤器。
4. 覆写 getNextVisualPositionFrom 返回哨兵值并写入 biasRet。

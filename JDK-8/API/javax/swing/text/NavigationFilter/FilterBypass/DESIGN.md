# javax.swing.text.NavigationFilter.FilterBypass — DESIGN

## 1. API 功能
NavigationFilter 的回调旁路（抽象类），用于在过滤器内部直接定位 Caret 而不重新触发过滤器。方法分组：
- 构造器：`protected FilterBypass()`。
- 查询：`getCaret()` 返回正在变化的 Caret。
- 定位：`setDot(int,Position.Bias)`、`moveDot(int,Position.Bias)`。

## 2. Java 用户如何使用
在 `NavigationFilter` 子类的 `setDot(FilterBypass fb, ...)` / `moveDot(...)` 方法里拿到 `fb`：`fb.getCaret()` 拿 Caret；`fb.setDot(dot, bias)` 或 `fb.moveDot(dot, bias)` 直接绕过过滤器定位；`super.setDot(fb, dot, bias)` 等价放行。

## 3. 开源代码实际用法调研（来源：JDK 源码）
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/DefaultFormatter.java`（`DefaultNavigationFilter`）：`fb.setDot(dot, bias); ... fb.moveDot(dot, bias);` —— 输入法组合态下用旁路直接定位。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/NavigationFilter.java`：`public void setDot(FilterBypass fb, int dot, Position.Bias bias) { fb.setDot(dot, bias); }` —— 默认放行就是调用旁路。
- `~/git/jdk8u/jdk/src/share/classes/javax/swing/text/DefaultCaret.java`（`getFilterBypass`）：`if (filterBypass == null) filterBypass = new DefaultFilterBypass(); return filterBypass;` —— Caret 内部持有旁路实例。

## 4. 测试用例设计 + 自动化实现
（11 断言）：抽象类形状（getCaret/setDot/moveDot）；实现具体 bypass（getCaret 返回 DefaultCaret）后 setDot/moveDot 驱动 caret 状态（dot/mark 精确变化）。

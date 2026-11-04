# javax.sound.sampled.LineUnavailableException — DESIGN

## 1. API 功能
`LineUnavailableException` 是受检异常（继承 `Exception`），表示请求的线路因资源占用/不兼容等原因不可用。方法：无参构造器与 `LineUnavailableException(String)`，其余继承自 `Throwable`（`getMessage/getCause/toString`）。

## 2. Java 用户如何使用
在 `AudioSystem.getLine(...)` / `Line.open()` 等声明抛出它的调用处 try/catch：`try { AudioSystem.getLine(info); } catch (LineUnavailableException e) { ... }`，通常读取 `e.getMessage()`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/sound/sampled/LineUnavailableException.java`：两个构造器直接 `super()`/`super(message)`。
- `test/jdk/javax/sound/sampled/Lines/GetLine.java`：`catch (LineUnavailableException e) { return PASS; }`。

## 4. 测试用例设计 + 自动化实现（`LineUnavailableExceptionTest.testBlackBox`）
- 白盒：公开类、继承 `Exception`、两个构造器。
- 黑盒：默认构造 `getMessage/getCause` 为 null 且非 RuntimeException；String 构造 `getMessage` 往返、`getCause` 为 null、`toString` 含消息。

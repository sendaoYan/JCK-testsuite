# java.awtFontMetrics 黑盒行为测试设计

## 1. API 功能
字体度量（BufferedImage 字体实测）：height==ascent+descent+leading、stringWidth==逐字符 charWidth 之和、charsWidth==stringWidth、bytesWidth ASCII 等价、宽度可加性、charWidth(int)==charWidth(char)、null 参数 NPE、越界 offset IndexOutOfBoundsException、大字号 height 增大、getMaxAdvance 有效。

## 2. Java 用户如何使用
```java
fm.stringWidth("ab") == fm.stringWidth("a")+fm.stringWidth("b")
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 FontMetrics.java：stringWidth 委托 charsWidth；关系式为规范要求。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：关系式四组；空串 0；NPE 三处；越界一处；字号单调；charWidth(int/char) 等价。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。

# java.awtim/InputMethodHighlight 黑盒行为测试设计

## 1. API 功能
输入法高亮：RAW_TEXT=0/CONVERTED_TEXT=1、4 个 *_HIGHLIGHT 常量状态、2/3/4 参构造、无 equals（恒等）、selected/variation 读回。

## 2. Java 用户如何使用
```java
InputMethodHighlight.UNSELECTED_RAW_TEXT_HIGHLIGHT
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 InputMethodHighlight.java；供 IME 绘制装饰。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：两常量；4 预定义高亮；构造器变体；无 equals 恒等。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。

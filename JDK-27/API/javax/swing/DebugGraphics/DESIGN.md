# javax.swing.DebugGraphics 测试设计

## 1. API 功能
图形调试包装（extends Graphics）：LOG/FLASH/BUFFERED/NONE 选项、
静态 flash 参数、logStream 重定向。

## 2. Java 用户如何使用
`repaintManager.setDoubleBufferingEnabled(false); label.setDebugGraphicsOptions(LOG_OPTION)`。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/DebugGraphics.java`；
JComponent.setDebugGraphicsOptions()；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- 常量精确：NONE=-1、LOG=1、FLASH=2、BUFFERED=4。
- 静态参数往返：flashTime/flashCount/flashColor/logStream（用后还原）。
- 行为验证：LOG_OPTION 下 drawLine 输出到 logStream——**易错**：日志文本是
  "Graphics(0-1) Drawing line: from (1, 2) to (3, 4)"（断言 "Drawing line" 而非 "drawLine"）。
- create() 派生新 DebugGraphics；setColor/getColor、setFont/getFont 透传。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。


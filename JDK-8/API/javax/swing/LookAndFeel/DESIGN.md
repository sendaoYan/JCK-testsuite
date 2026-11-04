# javax.swing.LookAndFeel 测试设计

## 1. API 功能
LaF 基类：描述元数据（getName/getID/isSupported...）+ 静态安装辅助
（installBorder/installColors/installProperty/makeKeyBindings/makeInputMap）。

## 2. Java 用户如何使用
自定义 LaF 继承并在 initComponentDefaults 里填 UIDefaults；UI 类用静态辅助装边框/颜色。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/LookAndFeel.java`；
MetalLookAndFeel.java；BasicLookAndFeel.java；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- Metal 身份锁定：name/id "Metal"、description 非空、非 native、supported、支持窗饰、toString 非空。
- makeKeyBindings 交替数组→KeyBinding[]；**易错**："A" 解析为 pressed A（getKeyStroke(String)），
  不是 typed A（getKeyStroke(char)）——断言用字符串重载比较。
- makeInputMap 同源解析。
- installBorder：从 defaults 取 UIResource 边框；uninstallBorder 只清 UIResource（非 UIResource 保留）、
  缺失默认时 no-op。
- installColors/installColorsAndFont 写 background/foreground/font；installProperty opaque。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。


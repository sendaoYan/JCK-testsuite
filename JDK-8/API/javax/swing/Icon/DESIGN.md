# javax.swing.Icon 测试设计

## 1. API 功能
图标接口：恰好 3 个方法（paintIcon/getIconWidth/getIconHeight）。

## 2. Java 用户如何使用
`label.setIcon(icon)`；自定义实现画矢量图形。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/Icon.java`；
ImageIcon.java；JLabel.setIcon()；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- 接口 shape：isInterface、无父、**恰好 3 个声明方法**。
- 契约经记录型实现验证：paintIcon 的 4 个参数（component 可为 null、graphics、x/y）逐字传递、
  恰调用一次。
- ImageIcon implements Icon；GrayFilter 不是 Icon。
- JLabel.setIcon 存同实例、setIcon(null) 清空。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。


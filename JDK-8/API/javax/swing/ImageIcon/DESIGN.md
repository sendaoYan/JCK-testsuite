# javax.swing.ImageIcon 测试设计

## 1. API 功能
基于 Image 的 Icon 实现：多构造器（byte[]/Image/String/URL±描述）、加载状态、
observer、序列化支持。

## 2. Java 用户如何使用
`new ImageIcon(getClass().getResource("/icon.png"), "save")`。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/ImageIcon.java`；
Icon.java；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- 空构造：image null、宽高 -1、描述 null、observer null。
- 合法数据：byte[](PNG)/Image 构造尺寸正确、MediaTracker.COMPLETE(8)、getImage 返回构造实例；
  描述构造器往返。
- 坏数据：非 PNG 字节→ERRORED(4)、宽高 -1。
- **易错**：setImage(null) 抛 NPE（8/27 一致）；setImage 换图后宽高即时更新。
- paintIcon(null component) headless 不抛且真实绘制像素（canvas 上验证 RED 落点）。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。


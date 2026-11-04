# java.awt.print.PageFormat 测试设计

## 1. API 功能
页面格式（Cloneable）：orientation（PORTRAIT=1/LANDSCAPE=0/REVERSE_LANDSCAPE=2）、Paper 持有、
getWidth/getHeight/getImageableWidth 等、getMatrix（设备变换矩阵）。

## 2. Java 用户如何使用
`PageFormat pf = job.defaultPage(); pf.setOrientation(PageFormat.LANDSCAPE); g2.transform(pf.getMatrix());`
按矩阵预变换后即可按物理坐标绘制。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/print/PageFormat.java`；
PrinterJob.defaultPage；`test/jdk/java/awt/print/`。

## 4. 测试用例设计 + 易错细节
- 默认 PORTRACT=1（枚举常量精确值）、默认 Paper LETTER。
- 三方向的 getMatrix 精确矩阵：portrait 恒等 [1,0,0,1,0,0]；landscape [0,-1,1,0,0,792]；
  reverse landscape [0,1,-1,0,612,0]（锁定平台一致行为）。
- **setPaper 克隆入参、getPaper 返回新副本**（修改传入 Paper 不影响格式，修改返回副本也不影响）。
- setOrientation 非法值抛 IllegalArgumentException。
- 方向切换时 getWidth/getHeight 互换、imageable 区随矩阵旋转。
- 不可序列化（NotSerializableException）。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。

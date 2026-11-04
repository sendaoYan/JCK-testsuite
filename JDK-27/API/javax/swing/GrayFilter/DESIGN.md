# javax.swing.GrayFilter 测试设计

## 1. API 功能
灰度 RGBImageFilter：构造(brighter, percent)，filterRGB 公式
gray=(int)((0.30r+0.59g+0.11b)/3) 后 darker: gray*(100-p)/100、brighter: 255-(255-gray)*(100-p)/100；
createDisabledImage 静态工厂。

## 2. Java 用户如何使用
`GrayFilter.createDisabledImage(icon.getImage())` 生成禁用态图标。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/GrayFilter.java`；
RGBImageFilter.java（java.awt.image）；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- **整数运算逐像素精确锁定**（filterRGB 可直调，无需整图过滤）：
  darker/50：红12 绿25 蓝4 白42 黑0 混(100,150,200)23；brighter/50：140/153/132/170/128/151；
  darker/20 混 32；brighter/20 混 84（注意 0.3r+0.59g+0.11b 先 /3 取整再按百分比整数算）。
- alpha 通道原样保留（半透明像素 alpha 不变）。
- createDisabledImage 尺寸保持。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。


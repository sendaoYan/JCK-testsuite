# java.awt.print.Paper 测试设计

## 1. API 功能
页面物理尺寸（单位 1/72 英寸，Cloneable）：默认 LETTER 612x792、setImageableArea/setSize、
getImageableX/Y/Width/Height、getHeight/getWidth、clone。

## 2. Java 用户如何使用
`Paper p = new Paper(); p.setSize(mm(210), mm(297)); p.setImageableArea(margin, margin,
w-2*margin, h-2*margin); pageFormat.setPaper(p);` 打印时可用区不含页边距。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/print/Paper.java`；
PageFormat/Pageable 调用点。

## 4. 测试用例设计 + 易错细节
- 默认 612x792、可打印区 72 边距 (72,72,468,648)。
- setSize/setImageableArea 读写往返；可打印区可延伸到页面边界（无校验）——负值/超界不抛异常。
- clone 是等值独立副本（改副本不影响原件）。
- **不可序列化**（NotSerializableException 断言，与 PageFormat 组合使用时需 clone 传递）。
- 易错点：单位是 points（72/inch），A4 是 595x842 而非毫米数。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。

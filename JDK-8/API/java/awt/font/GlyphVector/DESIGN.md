# java.awt.font.GlyphVector 测试设计

## 1. API 功能
字形向量（abstract）：createGlyphVector 的产物，保存字形码/位置/变换/逻辑与可视边界、
performDefaultLayout、getGlyphCharIndex/CharType/Code/Transform/Position/Bounds/Outline、
layoutFlags/equals。

## 2. Java 用户如何使用
`GlyphVector gv = font.createGlyphVector(frc, "abc");` 取 `getGlyphPosition(i)/getGlyphOutline(i)`
绘制自定义效果；`gv.setGlyphTransform(i, rot)` 做旋转字符；比对 `equals` 判断等价布局。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/font/GlyphVector.java`（StubVector/
SunLayoutEngine）；`test/jdk/java/awt/font/`。

## 4. 测试用例设计 + 易错细节
- 真实数据：`new Font("Dialog",PLAIN,12).createGlyphVector(FLOAT frc,"abc")` 的字形码连续
  （68/69/70）、位置 x=2(n+1) 单调、末位置==总 advance；bounds/outline 非 null。
- 简单 LTR 文本 layoutFlags==0、getJustificationInfo()==null、glyphTransform null。
- equals：同参两次构造相等；改 setGlyphPosition 后不等，performDefaultLayout 恢复后等。
- 变异与恢复：setGlyphPosition/setGlyphTransform 生效且 getGlyphPosition(3) 之外查询不受影响。
- 越界矩阵（getGlyphCharIndex 是例外——**返回入参原值不抛异常**，恒等映射）；其余
  getGlyphPosition/getGlyphTransform/getGlyphOutline 等越界抛 IndexOutOfBoundsException。
- 版本差异（javap 确认）：JDK-8 无参构造器为 public，JDK-9+ 收紧为 protected；
  setGlyphPosition 越界 JDK-8 抛 ArrayIndexOutOfBoundsException（IOOBE 子类），JDK-9+ 抛
  纯 IndexOutOfBoundsException——两侧分别按精确类型断言。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。

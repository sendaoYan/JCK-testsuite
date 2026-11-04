# java.awtCardLayout 黑盒行为测试设计

## 1. API 功能
卡片布局：hgap/vgap 内缩（(4,6,192,188)）、layoutContainer 后首卡可见其余隐藏、next/previous 翻转回绕、last/first/show(name)、show 未知名静默 no-op（观察）、隐藏卡仍保有布局 bounds、prefdim=孩子最大值+2×gap、maxdim Integer.MAX_VALUE、单卡翻转恒可见、外容器 IAE 'wrong parent for CardLayout'、removeLayoutComponent 不改 prefdim（no-op，观察）。

## 2. Java 用户如何使用
```java
((CardLayout)p.getLayout()).next(p);
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 CardLayout.java：内部 Vector 跟踪；checkLayout 校验 parent 布局为本对象。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：insets 矩形；可见性翻转全套；show no-op；prefdim/maxdim；单卡；wrong parent IAE；remove no-op。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。

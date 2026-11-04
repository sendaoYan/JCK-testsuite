# java.awtFlowLayout 黑盒行为测试设计

## 1. API 功能
流式布局：LEFT=0/CENTER=1/RIGHT=2/LEADING=3/TRAILING=4、默认 CENTER+gap 5/5+alignOnBaseline false；100 宽 3 个 30px 组件 → 行1 居中 (17,5)(52,5) 行2 (35,20)；LEFT 起始 hgap；RIGHT x=width-hgap-w；prefdim 单行打包 3*30+4*5=110 x 10+2*5=20；非 LayoutManager2（无 alignment/invalidate 方法）；removeLayoutComponent no-op。

## 2. Java 用户如何使用
```java
f.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 FlowLayout.java：直接遍历容器组件；仅实现 LayoutManager。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：常量；默认值；居中/左/右像素级；换行；prefdim；(20,20) 打包；非 LM2；remove no-op。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。

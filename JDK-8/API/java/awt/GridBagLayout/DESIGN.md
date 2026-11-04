# java.awtGridBagLayout 黑盒行为测试设计

## 1. API 功能
网格袋布局：columnWidths/rowHeights/columnWeights/rowWeights 公开可变字段；weightx 1:3 两格在 200 宽 → (0,45,60,10)/(60,45,140,10)；weighty=0 垂直居中 (0,45,200,10)；getConstraints 返回副本（字段保持）；getLayoutDimensions/getLayoutWeights/getLayoutOrigin；默认约束子组件居中 (20,20,10,10)；字符串约束 IAE 'cannot add to layout: constraints must be a GridBagConstraint'；alignment 0.5；maxdim 无界。

## 2. Java 用户如何使用
```java
gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH; p.add(c, gbc);
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 GridBagLayout.java：权重分配含最小尺寸底额（1:3 实得 60/140 而非 50/150——观察）。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：四字段可变；两格权重像素级；单格垂直居中；getConstraints 副本；布局信息三元组；默认约束居中；字符串 IAE；alignment/maxdim。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。

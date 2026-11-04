# java.awtLinearGradientPaint 黑盒行为测试设计

## 1. API 功能
多色线性渐变：端点读回、NO_CYCLE/SRGB 默认、getColors/getFractions 防御拷贝、IAE（乱序/重复/越界 fractions、长度不匹配、零长度渐变 'Start point cannot equalendpoint'）、NPE（null colors/fractions）、隐式 0/1 关键帧物化（3→5 色）、LINEAR_RGB、transparency、createContext。

## 2. Java 用户如何使用
```java
new LinearGradientPaint(0,0,100,0,new float[]{0,0.5f,1},new Color[]{red,white,blue})
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 LinearGradientPaint.java：首尾关键帧隐式物化；fractions 必须升序。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：端点读回；默认枚举；防御拷贝；IAE/NPE 矩阵含零长度消息；隐式关键帧 3→5 色；LINEAR_RGB；transparency；createContext。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。

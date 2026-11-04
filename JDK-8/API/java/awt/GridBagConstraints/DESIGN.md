# java.awtGridBagConstraints 黑盒行为测试设计

## 1. API 功能
网格袋约束：默认 gridx=gridy=RELATIVE(-1)/gridwidth=gridheight=1/weight 0/anchor=CENTER(10)/fill=NONE(0)/insets 零/ipad 0；RELATIVE=-1/REMAINDER=0；fill NONE=0/BOTH=1/HORIZONTAL=2/VERTICAL=3；anchor 罗盘 CENTER=10..WEST=17、PAGE/FIRST_LINE_START=23、BASELINE=0x100、ABOVE_BASELINE=0x400；全参构造读回；clone 深拷贝 insets；无 equals/hashCode（恒等——观察）；序列化保全部字段；sUID=-1000070633030801713L。

## 2. Java 用户如何使用
```java
GridBagConstraints c = (GridBagConstraints) proto.clone();
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 GridBagConstraints.java：clone 复制 insets；无 equals。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：默认值全套；全部常量精确；全参读回；clone 深拷贝；无 equals（反射+行为双锁）；序列化字段级；sUID。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。

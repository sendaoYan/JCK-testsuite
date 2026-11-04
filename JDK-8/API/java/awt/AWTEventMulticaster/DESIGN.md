# java.awtAWTEventMulticaster 黑盒行为测试设计

## 1. API 功能
事件多播器：add(null,null)=null、add(null,l)=l、链式分发计数、重复监听器计数、remove 语义（含重复移除）、3 层链中段摘除、ComponentListener 族、实现全部监听接口、异常沿链传播同一异常对象。

## 2. Java 用户如何使用
```java
AWTEventMulticaster.add(l1, l2).componentResized(ev)
```

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 AWTEventMulticaster.java：二叉链表；remove 返回新链。

## 4. 测试用例设计 + 自动化实现
testContract()（本任务深化）：
testContract：null 组合；分发计数；重复监听 2→1→null；中段摘除；Component 族接口；instanceof 全接口；同一异常对象传播。
另保留既有 testWhiteBox/testBlackBox 全部断言。headless（env -u DISPLAY）可跑，无 @key 标签。

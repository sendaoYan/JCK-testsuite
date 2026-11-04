# javax.management.QueryExp 黑盒行为测试设计

## 1. API 功能
- 接口：`apply(ObjectName)`、`setMBeanServer(MBeanServer)`。

## 2. Java 用户如何使用
MBeanServer.queryNames/queryMBeans 对每个候选 ObjectName 调 apply 决定是否匹配。
```java
boolean match = queryExp.apply(candidate);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/query/*Test.java`。
- 实现权威：`src/java.management/share/classes/javax/management/QueryExp.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：Query.eq/and 等组合表达式实现 QueryExp，注册 MBean 后 queryNames 匹配预期对象。
- 白盒：public 接口、extends Serializable、含 apply/setMBeanServer。

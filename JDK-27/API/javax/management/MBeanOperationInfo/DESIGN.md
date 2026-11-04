# javax.management.MBeanOperationInfo 黑盒行为测试设计

## 1. API 功能
- 常量：`INFO`(0)、`ACTION`(1)、`ACTION_INFO`(2)、`UNKNOWN`(3)。
- 构造：`(String name, String description, MBeanParameterInfo[] signature, String type, int impact)` 及带 Descriptor 重载。
- 查询：`getReturnType()`、`getSignature()`、`getImpact()`；`clone()`、`equals()`。

## 2. Java 用户如何使用
MBeanInfo 描述操作，管理工具据此展示并调用操作。
```java
MBeanOperationInfo oi = new MBeanOperationInfo("reset", "reset", params, "int", ACTION);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/mxbean/MBeanOperationInfoTest.java`、`OperationImpactTest.java`。
- `test/jdk/javax/management/Introspector/FeatureOrderTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanOperationInfo.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getReturnType/getSignature/getImpact 返回入参；impact 常量值正确；clone 等值；equals 敏感。
- 白盒：public 类、父类 MBeanFeatureInfo、implements Cloneable、含 getReturnType/getSignature/getImpact。

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

## 5. 深化（批 B 追加）
- `Method` 构造：impact 默认 UNKNOWN、参数名 p1/p2、参数描述为空串、方法注解 @DescriptorKey 进入操作描述符。
- impact 常量 INFO/ACTION/ACTION_INFO/UNKNOWN == 0..3。
- 【版本差异】JDK-8 不校验 impact 取值范围：99 与 -1 均被原样保留（getImpact() 返回原值）；JDK-27 对越界值抛 IllegalArgumentException。
- getSignature：空签名共享同一空数组实例，非空为防御性拷贝；descriptor 构造 round-trip。
- equals：name/returnType/signature/description/impact/descriptor 全参与；hashCode 仅 name+returnType。
- 序列化 round-trip。MBeanOperationInfo(String, Method) 描述符来自方法注解。

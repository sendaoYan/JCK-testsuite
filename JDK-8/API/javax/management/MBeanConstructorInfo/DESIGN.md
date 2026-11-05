# javax.management.MBeanConstructorInfo 黑盒行为测试设计

## 1. API 功能
- 构造：`(String name, String description, MBeanParameterInfo[] signature)` 及带 Descriptor 的重载。
- 查询：`getSignature()`、继承的 name/description/getDescriptor；`clone()`、`equals()`。

## 2. Java 用户如何使用
MBeanInfo 描述构造器，供管理控制台展示如何构造 MBean。
```java
MBeanConstructorInfo ci = new MBeanConstructorInfo("Sample", "ctor", params);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanInfo/NullInfoArraysTest.java`：空参数数组。
- `test/jdk/javax/management/Introspector/FeatureOrderTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanConstructorInfo.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getSignature 返回入参数组；clone 返回等值副本；equals 对 name/description/signature 敏感；带 Descriptor 构造后 getDescriptor 返回该值。
- 白盒：public 类、父类 MBeanFeatureInfo、implements Cloneable、含 getSignature。

## 5. 深化（批 B 追加）
- `Constructor` 反射构造：name 等于二进制类名、参数名 p1/p2。
- 空签名共享同一空数组实例；descriptor 构造 round-trip（equals 保持）。
- equals：name、signature、description、descriptor 全参与；hashCode = name ^ Arrays.hashCode(signature)。
- 序列化 round-trip。
- 版本差异：无。

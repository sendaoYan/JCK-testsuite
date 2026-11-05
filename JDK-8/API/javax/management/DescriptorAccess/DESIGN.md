# javax.management.DescriptorAccess 黑盒行为测试设计

## 1. API 功能
- 接口：`setDescriptor(Descriptor)`，extends DescriptorRead。可写描述符的元数据需实现它；标准 ModelMBean 元数据（ModelMBean*Info）是真实实现者。

## 2. Java 用户如何使用
ModelMBean 在运行期改写属性/操作描述符。
```java
ModelMBeanAttributeInfo mai = ...;
mai.setDescriptor(d);
```

## 3. 开源代码实际用法调研
- 实现权威：`.../modelmbean/ModelMBeanAttributeInfo.java`（setDescriptor 契约）。

## 4. 测试用例设计 + 自动化实现
- 黑盒：接口结构 + ModelMBean 实现者闭环。
- 白盒：单方法接口、extends DescriptorRead。

## 5. 深化（批 B 追加）
- 恰声明一个方法 setDescriptor；extends DescriptorRead。
- 只读边界：普通 MBeanFeatureInfo/MBean*Info 不是 DescriptorAccess（仅 DescriptorRead）。
- ModelMBeanAttributeInfo/OperationInfo/ConstructorInfo/NotificationInfo 四类均实现 DescriptorAccess。
- ModelMBeanAttributeInfo 契约：默认描述符 name=特性名、descriptorType=attribute；setDescriptor 克隆存储并默认 displayName=特性名；name 不匹配或 descriptorType 错误 → RuntimeOperationsException(IAE)；setDescriptor(null) 重建默认描述符（name/descriptorType/displayName）。
- ModelMBeanOperationInfo 以 descriptorType=operation round-trip；匿名实现 set/get 恒等。
- 版本差异：无。

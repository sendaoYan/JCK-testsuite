# javax.management.modelmbean.ModelMBeanInfoSupport — 设计案

## 1. API 功能
- 具体类，继承 `MBeanInfo` 并实现 `ModelMBeanInfo`。
- 构造器：标准 `(className,description,attrs,ctors,ops,notifs)` 与基于 `ModelMBeanInfo` 的拷贝构造、带 Descriptor 的完整构造。
- 默认 MBean 描述符含 `name`、`descriptorType=mbean`、`displayName`、`persistPolicy=never`。

## 2. Java 用户如何使用
作为 `RequiredModelMBean` 的元数据来源，一次性描述属性/构造器/操作/通知并附带描述符。
```java
ModelMBeanInfoSupport info = new ModelMBeanInfoSupport(
    "MyMBean", "desc", attrs, ctors, ops, notifs);
RequiredModelMBean mb = new RequiredModelMBean(info);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/javax/management/modelmbean/ModelMBeanInfoSupportTest.java` 系统覆盖描述符访问、setMBeanDescriptor、clone 与异常路径。
- 实现权威：`src/java.management/share/classes/javax/management/modelmbean/ModelMBeanInfoSupport.java`。

## 4. 测试用例设计
- 白盒：继承 MBeanInfo、实现 ModelMBeanInfo、多个 public 构造器。
- 黑盒：基础 MBeanInfo 契约（className/description/各数组长度）；MBean 描述符默认字段；getDescriptor 按 name+type 与单参数检索；getDescriptors("attribute") 与 getDescriptors(null)；按类型便捷查询；setMBeanDescriptor/setDescriptor 更新；clone 独立；拷贝构造；异常路径（getDescriptor(null)、setDescriptor 非法类型）。

# javax.management.DescriptorAccess 黑盒行为测试设计

## 1. API 功能
- 接口：`getDescriptor()`、`setDescriptor(Descriptor)`。

## 2. Java 用户如何使用
MBean 或 DynamicMBean 实现此接口以暴露/替换自己的元数据描述符。
```java
if (bean instanceof DescriptorAccess) {
    ((DescriptorAccess) bean).setDescriptor(d);
}
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/descriptor/DescriptorTest.java`：descriptor 读写入口。
- 实现权威：`src/java.management/share/classes/javax/management/DescriptorAccess.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：用简单实现类验证 getDescriptor 返回 setDescriptor 设置的对象；setDescriptor(null) 后 getDescriptor 为 null。
- 白盒：public 接口、含 getDescriptor/setDescriptor。

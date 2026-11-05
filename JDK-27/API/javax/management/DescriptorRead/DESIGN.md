# javax.management.DescriptorRead 黑盒行为测试设计

## 1. API 功能
- 接口：`getDescriptor()`。

## 2. Java 用户如何使用
只读方式获取 MBean 元数据描述符；实现类提供 descriptor。
```java
Descriptor d = ((DescriptorRead) bean).getDescriptor();
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/descriptor/*Test.java`：descriptor 读取。
- 实现权威：`src/java.management/share/classes/javax/management/DescriptorRead.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：用简单实现类验证 getDescriptor 返回构造时设置的描述符。
- 白盒：public 接口、含 getDescriptor。

## 5. 深化（批 B 追加）
- 元数据族七类均 instanceof DescriptorRead 且描述符非 null（默认空）。
- MBeanInfo 提供的 ImmutableDescriptor("k=v") 经构造保留。
- 版本差异：无。

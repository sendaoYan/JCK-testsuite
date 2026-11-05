# org.omg.CORBA_2_3.portable.OutputStream 契约设计

## API 功能
2.3 值流写通道：4 个 write_value 重载 + write_abstract_interface。

## 参考来源
- OMG 2.3 Java 映射；jdk8u372-b07 实测。

## 测试用例设计
- abstract、super、方法名集合、共 5 方法（4+1）。

## 易错规范细节
- write_value(Serializable, Class/String/BoxedValueHelper) 各自对应不同 rep-id 来源。

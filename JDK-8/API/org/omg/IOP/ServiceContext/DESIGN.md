# org.omg.IOP.ServiceContext 契约设计

## API 功能
final 值类（IDLEntity）：`int context_id` + `byte[] context_data`；() 与 (int,byte[]) 构造。

## 参考来源
- OMG IOP ServiceContext；jdk8u372-b07 实测。

## 测试用例设计
- 默认 (0,null)；全参回读；别名探测（无防御拷贝）。
- ServiceContextHelper.write/read 往返（id 0xEF + 0xAA 字节）。
- write(null context_data) → NPE；final/两字段/IDLEntity。

## 易错规范细节
- context_id 语义上是 ServiceId 家族（0/1/14/17）。

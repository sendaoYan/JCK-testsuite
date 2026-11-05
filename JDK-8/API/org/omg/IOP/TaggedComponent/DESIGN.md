# org.omg.IOP.TaggedComponent 契约设计

## API 功能
final 值类（IDLEntity）：`int tag` + `byte[] component_data`；() 与 (int,byte[]) 构造。

## 参考来源
- OMG IOP TaggedComponent；jdk8u372-b07 反射+行为实测。

## 测试用例设计
- 默认构造 (0,null)；全参回读。
- 防御拷贝双向实测：**无拷贝**——构造后改原数组字段可见；读出的数组改动写回亦可见；`component_data == 原数组`（引用同一）。
- CDR 往返（TaggedComponentHelper.write/read）：tag 0xABCD + 2 字节精确；往返产物是新数组（与源不别名）。
- write(null data) → NPE。
- 类形态：final、两字段、IDLEntity。

## 易错规范细节
- 千万别断言“构造做了防御拷贝”——JDK 生成的 IDL 类直接存引用。
- wire 往返确实新分配数组，勿与构造别名混为一谈。

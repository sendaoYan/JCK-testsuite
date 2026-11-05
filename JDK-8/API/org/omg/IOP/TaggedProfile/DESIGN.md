# org.omg.IOP.TaggedProfile 契约设计

## API 功能
final 值类（IDLEntity）：`int tag` + `byte[] profile_data`；() 与 (int,byte[]) 构造。

## 参考来源
- OMG IOP TaggedProfile；jdk8u372-b07 实测。

## 测试用例设计
- 默认 (0,null)；全参回读；别名探测（无防御拷贝，引用同一）。
- TaggedProfileHelper.write/read 往返 tag+data 精确。
- write(null profile_data) → NPE；final/两字段/IDLEntity。

## 易错规范细节
- 与 TaggedComponent 同构但字段名不同（profile_data vs component_data）。

# org.omg.IOP.IOR 契约设计

## API 功能
final 值类（IDLEntity）：`String type_id` + `TaggedProfile[] profiles`；() 与 全参构造。

## 参考来源
- OMG IOP IOR；jdk8u372-b07 离线 CDR 实测。

## 测试用例设计
- 默认 (null,null)；全参回读；数组引用别名（改源数组元素可见）。
- IORHelper.write→read CDR 真往返：type_id equals、profiles 逐 tag/逐 data 相等；空 profiles 序列往返。
- write(null profiles) → NPE。
- Any 通道 insert/extract 往返。
- final/IDLEntity/两字段。

## 易错规范细节
- profiles 数组本身不拷贝（别名）；Helper.read 产物是全新对象树。
- CDR 布局：string type_id + sequence<TaggedProfile>（长度前缀 + 每 profile tag int + octet 序列）。

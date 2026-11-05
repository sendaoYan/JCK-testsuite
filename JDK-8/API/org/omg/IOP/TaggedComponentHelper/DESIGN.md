# org.omg.IOP.TaggedComponentHelper 契约设计

## API 功能
struct Helper：insert/extract/read/write/type/id。

## 参考来源
- OMG IOP；jdk8u372-b07 实测。

## 测试用例设计
- id() 精确；type tk_struct（2 成员）。
- wire 往返 tag 0xABCD+bytes；Any 往返。
- 负例：insert(null)→NPE；extract(空)→MARSHAL；write(null data)→NPE。

## 易错规范细节
- CDR 布局：tag(long)+octet 序列（长度前缀）。

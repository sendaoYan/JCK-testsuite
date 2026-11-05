# org.omg.IOP.TaggedProfileHelper 契约设计

## API 功能
struct Helper 四件套。

## 参考来源
- OMG IOP；jdk8u372-b07 实测。

## 测试用例设计
- id()/type tk_struct 精确；wire 往返；Any 往返；write(null data)→NPE。

## 易错规范细节
- profile_data 与 component_data 字段名差异不影响 Helper 形态。

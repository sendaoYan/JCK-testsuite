# org.omg.IOP.ServiceContextHelper 契约设计

## API 功能
struct Helper 四件套。

## 参考来源
- OMG IOP；jdk8u372-b07 实测。

## 测试用例设计
- id() 精确；tk_struct；wire 往返（0xEF/0xAA）；Any 往返；insert(null)→NPE。

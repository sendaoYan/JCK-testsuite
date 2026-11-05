# org.omg.IOP.ProfileIdHelper 契约设计

## API 功能
别名 Helper（typedef unsigned long -> int）。

## 参考来源
- OMG IOP；jdk8u372-b07 实测。

## 测试用例设计
- id() 精确；type().kind()==tk_alias；wire 往返 0/1/0x7FFFFFFF；Any 往返。

## 易错规范细节
- 别名 TypeCode 的 kind 是 tk_alias，不是底层 tk_ulong。

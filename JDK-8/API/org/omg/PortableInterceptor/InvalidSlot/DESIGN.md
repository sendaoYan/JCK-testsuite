# org.omg.PortableInterceptor.InvalidSlot 契约设计

## API 功能
final UserException：()/  (String)；Current.get_slot/set_slot 越界信号。

## 参考来源
- OMG PI；jdk8u372-b07 实测。

## 测试用例设计
- final/2 构造器/toString 三形态精确/getMessage 前缀 repo id。
- Helper wire 往返（丢消息）/Any 往返/id 精确/type tk_except。

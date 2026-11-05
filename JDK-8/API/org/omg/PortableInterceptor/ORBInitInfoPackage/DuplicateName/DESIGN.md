# ORBInitInfoPackage.DuplicateName 契约设计

## API 功能
final UserException：成员 `String name`；()/  (String name)/  (String reason, String name) 三构造。

## 参考来源
- OMG PI ORBInitInfo；jdk8u372-b07 实测。

## 测试用例设计
- 三构造链：无参 name==null；(String) 设 name；(reason,name) 二者都设。
- toString/getMessage 精确（repo id + 两空格 + reason）。
- Helper wire 往返保 name 丢 reason；Any 往返保 name。

## 易错规范细节
- (String) 单参构造设的是 **name** 成员（不是 reason），toString 尾部会出现 name。

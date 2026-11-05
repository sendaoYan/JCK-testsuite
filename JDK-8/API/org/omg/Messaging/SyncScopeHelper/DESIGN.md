# org.omg.Messaging.SyncScopeHelper 契约设计

## API 功能
别名 Helper（typedef unsigned short SyncScope）。

## 参考来源
- OMG Messaging 规范；jdk8u372-b07 实测。

## 测试用例设计
- id() 精确 "IDL:omg.org/Messaging/SyncScope:1.0"；type().kind()==tk_alias。
- wire 往返 1/Short.MAX_VALUE；Any insert/extract 往返。

## 易错规范细节
- tk_alias 而非 tk_ushort（别名不展开）。

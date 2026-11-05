# org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS 契约设计

## API 功能
ComponentId 常量接口：`int value = 3`（组件 tag：备用 IIOP 地址）。

## 参考来源
- OMG IOP 规范；jdk8u372-b07 实测。

## 测试用例设计
- 值精确 ==3；单字段；final；int。

## 易错规范细节
- 组件数据为 Host(string)+Port(ushort) 封送，本测试只锁常量值。

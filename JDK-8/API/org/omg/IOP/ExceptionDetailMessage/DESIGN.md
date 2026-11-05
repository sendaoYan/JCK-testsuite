# org.omg.IOP.ExceptionDetailMessage 契约设计

## API 功能
ServiceId 常量接口：`int value = 14`（异常详细消息服务上下文）。

## 参考来源
- OMG IOP ServiceId 表；jdk8u372-b07 实测。

## 测试用例设计
- 值精确 ==14；单字段；final；int。

## 易错规范细节
- 14 是正式分配号（非 SUN 保留段）。

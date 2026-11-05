# org.omg.IOP.ServiceContextListHelper 契约设计

## API 功能
序列 Helper（ServiceContext[]）。

## 参考来源
- OMG IOP；jdk8u372-b07 实测。

## 测试用例设计
- id() 精确；type().kind()==tk_alias（序列 typedef 的别名类型码）。
- wire 往返（2 元素+空序列）；Any 往返。

## 易错规范细节
- 序列 Helper 的 type 是 tk_alias 而非 tk_sequence（别名展开在 content_type）。

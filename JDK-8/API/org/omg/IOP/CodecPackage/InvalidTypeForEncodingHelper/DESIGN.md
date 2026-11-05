# org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper 契约设计

## API 功能
异常 Helper：read/write/insert/extract/type/id。

## 参考来源
- jdk8u372-b07 实测。

## 测试用例设计
- id() 精确；type()==tk_except；wire 往返；Any 往返（Any type==tk_except）；同流双异常连续往返。

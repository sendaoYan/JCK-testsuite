# org.omg.IOP.CodecOperations 契约设计

## API 功能
方法面契约（4 方法 + 异常声明）。

## 参考来源
- javap jdk8u372-b07。

## 测试用例设计
- 方法名集合精确 {encode,decode,encode_value,decode_value}；各方法 throws 子句精确（InvalidTypeForEncoding/FormatMismatch/FormatMismatch+TypeMismatch）。

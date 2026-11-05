# org.omg.IOP.Codec 契约设计

## API 功能
编码器接口（encode/encode_value/decode/decode_value）。

## 参考来源
- OMG IOP Codec 规范；jdk8u372-b07 离线实测（全部行为为本机探测锁定）。

## 测试用例设计
- 正闭环：long 4242 encode→12 字节（首字节 0=big-endian）→decode extract_long==原值；string 往返；encode_value 8 字节（无 TypeCode）→decode_value(tk_long) 回读；负数往返。
- 负例矩阵（精确异常）：
  - encode(null) / encode_value(null) / decode(null) / decode_value(bytes,null tc) → org.omg.CORBA.BAD_PARAM（SUN 201）
  - decode(空数组)/(byte[3])、decode_value(过短/垃圾 bytes, tc) → org.omg.IOP.CodecPackage.FormatMismatch
- 差异锁定：decode_value(类型不匹配的合法字节, 其他 primitive tc) 在 jdk8 **不抛 TypeMismatch**，静默按给定 tc 重解释（返回 Any 类型=请求 tc）。
- CodecOperations 方法面恰 4 方法。

## 易错规范细节
- enc[0]==0 是封装头字节序标志（0=大端），不是数据。
- 别断言 decode_value 必抛 FormatMismatch/TypeMismatch——实现是宽松重解释。

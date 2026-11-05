# org.omg.IOP.ENCODING_CDR_ENCAPS 契约设计

## API 功能
EncodingFormat 常量接口：`short value = 0`。CDR 封装编码——离线 CodecFactory 唯一接受的 format。

## 使用方式
`cf.create_codec(new Encoding(ENCODING_CDR_ENCAPS.value, (byte)1, (byte)2))`。

## 参考来源
- OMG IOP CodecFactory 规范；jdk8u372-b07 离线实测。

## 测试用例设计
- 值精确 ==0；类型 short；单字段；final。
- 正例：create_codec(0,1,2) 成功。
- 负例：create_codec(77,1,2) → org.omg.IOP.CodecFactoryPackage.UnknownEncoding。

## 易错规范细节
- format 是 short（EncodingFormat），major/minor 是 byte。
- jdk8u372 对 format!=0 一律 UnknownEncoding；对 (0,9,9) 等任意 GIOP 版本号不校验。

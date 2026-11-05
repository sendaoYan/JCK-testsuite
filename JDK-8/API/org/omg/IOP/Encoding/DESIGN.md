# org.omg.IOP.Encoding 契约设计

## API 功能
final 值类（IDLEntity）：`short format` + `byte major_version` + `byte minor_version`。

## 参考来源
- OMG IOP CodecFactory；jdk8u372-b07 离线实测。

## 测试用例设计
- 默认 (0,0,0)；全参回读；字段可变。
- 正例：create_codec(ENCODING_CDR_ENCAPS=0,1,2) 成功。
- 负例：format=1 → UnknownEncoding；format=0 且版本 (9,9) 仍成功（版本号不校验）。

## 易错规范细节
- JDK 只实现 ENCODING_CDR_ENCAPS(0)；GIOP 版本也校验：(0,9,9) 同样抛 UnknownEncoding。

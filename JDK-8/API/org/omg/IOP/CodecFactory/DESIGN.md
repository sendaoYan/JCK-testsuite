# org.omg.IOP.CodecFactory 契约设计

## API 功能
Codec 工厂（create_codec(Encoding)）。

## 参考来源
- OMG IOP；jdk8u372-b07 离线实测。

## 测试用例设计
- 正例：GIOP 1.2 与 1.1 codec 均可创建并完成 encode/decode 往返；重复创建独立可用。
- 负例：format=77/1/-1 → UnknownEncoding；GIOP 版本 (9,9) → UnknownEncoding；null Encoding → NPE。

## 易错规范细节
- 版本号也校验（非仅 format）；null Encoding 是 BAD_PARAM 而非 NPE。

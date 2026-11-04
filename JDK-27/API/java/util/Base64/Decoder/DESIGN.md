# java.util.Base64.Decoder 测试设计案（JDK-27）

> 覆盖目标：完整契约：解码向量、非法输入 IllegalArgumentException、MIME 忽略非法字符差异、wrap 流解码、round-trip。

## 1. API 功能

- decode(String)/decode(byte[])/decode(byte[],byte[])
- decode(ByteBuffer)/decode(In,Byte,boolean)
- wrap(InputStream) 解码流

## 2. 使用方式

Base64.getDecoder().decode(...) 与 Encoder round-trip；非法字符 → IllegalArgumentException（MIME 解码器宽容）。

## 3. 参考来源

- Oracle Java 17+/27 javadoc `java/util/Base64.Decoder`（RFC 4648）
- JDK 源码 Base64.Decoder（非法长度/字符判定）

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 已知向量解码精确 | testDecoder |
| P0 | 非法字符/非法长度 → IllegalArgumentException | testDecoderContracts |
| P0 | MIME 解码器忽略换行等非法字符 | testDecoderContracts |
| P1 | wrap(InputStream) 流式 round-trip | testDecoderContracts |
| P0 | 契约深化：- | testDecoderContracts |

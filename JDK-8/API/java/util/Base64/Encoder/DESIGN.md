# java.util.Base64.Encoder 测试设计案（JDK-8）

> 覆盖目标：完整契约：基本/MIME/URL 编码向量、withoutPadding、encode 到 byte[]/ByteBuffer、encodeToString、OutputStream 包装。

## 1. API 功能

- encode(byte[])/encode(byte[],byte[])/encodeToString(byte[])
- encode(ByteBuffer)/encode(In,Byte,boolean)
- withoutPadding()；wrap(OutputStream) 编码流

## 2. 使用方式

Base64.getEncoder()（getMimeEncoder/getUrlEncoder 在 Base64 测试）→ encodeToString 已知向量（如 foobar 三段）。

## 3. 参考来源

- Oracle Java 8 javadoc `java/util/Base64.Encoder`（RFC 4648 向量表）
- JDK 源码 Base64.Encoder 实现

## 4. 测试用例设计

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 已知向量三段编码精确 | testEncoder |
| P0 | withoutPadding 后无 '=' 填充 | testEncoderContracts |
| P0 | wrap(OutputStream) 流式与一次性结果一致 | testEncoderContracts |
| P1 | ByteBuffer 进出位限（position/limit 尊重） | testEncoderContracts |
| P0 | 契约深化：- | testEncoderContracts |

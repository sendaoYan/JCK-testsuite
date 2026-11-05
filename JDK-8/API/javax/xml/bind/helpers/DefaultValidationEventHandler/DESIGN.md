# javax.xml.bind.helpers.DefaultValidationEventHandler 契约覆盖设计（JDK-8）

## 1. API 功能
JAXB 默认校验事件处理器（Unmarshaller/Marshaller 未安装自定义 handler 时的行为来源）：**WARNING 继续（返回 true）、ERROR 与 FATAL_ERROR 中止（返回 false）**；同时把每个事件以 `[SEVERITY]: message\n     Location: ...` 形式打印到 System.err（无害噪音，测试不断言日志）。

## 2. 使用方式
```java
Unmarshaller u = ctx.createUnmarshaller();
// 不设置 handler 时等效于：
u.setEventHandler(new DefaultValidationEventHandler());
// 显式恢复默认：
u.setEventHandler(null);   // RI 内部落回新 DefaultValidationEventHandler
```

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/helpers/DefaultValidationEventHandler.html
- JSR 222 (JAXB 2.2) 规范第 6 章
- OpenJDK 8u RI 源码 `javax/xml/bind/helpers/DefaultValidationEventHandler.java`
- 本仓库测试：`DefaultValidationEventHandlerTest.java`

## 4. 测试用例设计 + 易错规范细节
- 裁决矩阵（核心，实测锁定）：handleEvent(WARNING)==true、handleEvent(ERROR)==false、handleEvent(FATAL_ERROR)==false。
- 稳定性：重复调用同一事件裁决不变；裁决与 message/locator/linked exception 载荷无关（构造带 linked 的 WARNING/FATAL 再测）。
- 无状态性：新实例与旧实例裁决矩阵一致。
- 接口与结构：implements ValidationEventHandler、单一公共 no-arg 构造器。
- 负例：`handleEvent(null)` → **IllegalArgumentException**（实测锁定）。
- 易错点：运行时会向 System.err 打印事件（含 "Location: unavailable"），jtreg 下无害，不要尝试断言或抑制。

# javax.xml.ws.handler.MessageContext.Scope — 设计文档

## 一、API 功能
`Scope` 是枚举（final），恰 2 个常量：`APPLICATION`（属性对服务端/客户端实现可见）、`HANDLER`（属性仅处理器链可见）。配合 `MessageContext.setScope/getScope` 划定属性可见性边界，防止处理器私有状态泄漏到业务实现。

## 二、使用方式
`ctx.put(key, value)` 默认 APPLICATION 域；处理器私有属性应随后 `ctx.setScope(key, Scope.HANDLER)`；`ctx.getScope(key)` 读域。测试验证枚举次序/ordinal/名称/valueOf 精确性、真实域切换回读、负例异常。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.handler.MessageContext.Scope
- JSR 224 §9.4
- OpenJDK 8 内置 RI（枚举无独立实现，由 MessageContextImpl 消费）
- 本仓库测试：JDK-8/API/javax/xml/ws/handler/MessageContext/Scope/ScopeTest.java

## 四、测试用例设计 + 易错规范细节
- values() 恰 2 且顺序 [APPLICATION, HANDLER]；ordinal 0/1；name/valueOf 大小写敏感；NEVER/""/null/lowercase → IllegalArgumentException/NPE。
- getDeclaredFields 实测 3 个（含编译器合成的 $VALUES）——统计真实字段须过滤 isSynthetic()。
- 枚举 final 且隐式继承 java.lang.Enum；不可再子类化。
- 真实上下文上 setScope/getScope 往返：put 后默认 APPLICATION，切 HANDLER 可读回。
- 易错点：valueOf(null) 抛 NullPointerException 而非 IAE；合成字段计数是最常见误判。

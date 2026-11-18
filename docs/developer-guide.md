# Babel 开发者指南

本文面向需要依赖 Babel 的模组作者，说明对外稳定接口、事件时机与扩展流程。示例基于 Forge 1.20.1 与 Java 17。

## 目录

- [依赖引入](#依赖引入)
- [稳定性约定](#稳定性约定)
- [事件一览](#事件一览)
- [打开收藏品](#打开收藏品)
- [运行时读写收藏品](#运行时读写收藏品)
- [元素损伤](#元素损伤)
- [目标生命与护盾](#目标生命与护盾)
- [难度](#难度)
- [掉落接入](#掉落接入)
- [端侧与线程](#端侧与线程)
- [常见扩展场景](#常见扩展场景)
- [调试](#调试)
- [版本与兼容策略](#版本与兼容策略)
- [开发者 FAQ](#开发者-faq)
- [兼容说明](#兼容说明)

## 依赖引入

发布到本地 Maven 仓库后可这样依赖：

```groovy
dependencies {
    implementation fg.deobf("com.susen36.babel:babel-lib:1.2.6")
}
```

或使用 JitPack 构建（以构建页显示的实际坐标为准）：

```groovy
repositories {
    maven { url = 'https://jitpack.io' }
}

dependencies {
    implementation fg.deobf('com.github.SuSen36:babel-lib:1.20.1-SNAPSHOT')
}
```

## 稳定性约定

| 包 | 定位 | 升级兼容性 |
|---|---|---|
| `com.susen36.babel.api` | 对外事件与实体接口 | 稳定，可放心依赖 |
| `collectible` | 收藏品框架与物品基类 | 稳定 |
| `elemental` | 元素损伤实现与 `EPManager` | 稳定 |
| `capability` | EP / Health 两类 Capability | 稳定 |
| `difficulty` | 难度等级与倍率工具 | 稳定 |
| `client`、`mixin`、`datagen`、`init`、`network`、`event` | 内部实现 | 可能随版本变动 |

属性全部注册在 `babel` 命名空间下，通过 Forge 标准 `Attribute` 查询即可，无需直接访问内部类。

## 事件一览

| 事件 | 触发时机 | 可否取消 / 修改 |
|---|---|---|
| `ElementEvent.Hurt` | 元素损伤结算前 | 可取消；可改 `amount` |
| `ElementEvent.PostHurt` | 元素损伤结算后 | 只读 |
| `ElementEvent.Heal` | 元素损伤恢复前 | 可取消；可改 `amount` |
| `ElementEvent.Burst` | 损伤攒满爆条时 | 只读 |
| `ResistDamageEvent.Defense` | 物理防御参与减伤 | 可改生效属性值 |
| `ResistDamageEvent.MagicResistance` | 法术抗性参与减伤 | 可改生效属性值 |
| `DodgeEvent` | 物理或法术闪避触发 | 只读 |
| `LifePointConsumeEvent.Pre` | 目标生命扣除前 | 可取消 |
| `LifePointConsumeEvent.Post` | 护盾 / 目标生命扣除时 | 可取消 |
| `LivingAddEffectEvent` | 实体获得药水效果 | 可整体替换效果实例 |
| `CollectibleEvent.Used` / `.Removed` / `.LayerChanged` | 收藏品使用 / 移除 / 层数变化 | 只读 |
| `CollectibleDropEvent` | 实体死亡结算收藏品掉落 | 可改概率，设 0 阻止 |
| `CollectibleRegisterEvent` | 收藏品注册表就绪 | 注入收藏品 |
| `OriginiumIngotDropEvent` | 结算源石锭掉落 | 可改倍率，设 0 阻止 |
| `LostAndFoundEvent.Pre` / `.Post` | 坎诺特兑换前 / 后 | `Pre` 可取消、可替换获得藏品 |

所有事件都发布在 Forge 事件总线（`MinecraftForge.EVENT_BUS`），除 `CollectibleRegisterEvent` 发布在 mod 事件总线。

## 打开收藏品

`CollectibleBuilder.create(modid, modBus)` 在 mod 构造阶段调用，内部会注册 `DeferredRegister<Item>` 并触发 `CollectibleRegisterEvent`，因此收藏品字段可直接写成 `public static final`：

```java
public final class MyCollectibles {

    private static final CollectibleBuilder BUILDER =
            CollectibleBuilder.create("mymod", FMLJavaModLoadingContext.get().getModEventBus());

    public static final RegistryObject<Item> SAMPLE_RELIC = BUILDER.registerCollectible(
            "sample_relic",                        // 注册名 → mymod:sample_relic
            25,                                    // 使用耗时（tick）
            false,                                 // 是否可无限次重复使用
            CollectibleTiers.RARE,                 // 稀有度
            new CollectibleItem.Levels(0, 3, 1),   // 层数 min / max / 默认
            CollectibleActivation.standard(),      // 音效、粒子与是否自动使用
            (stack, level, player) -> {            // 激活效果
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 0));
            });
}
```

已有注册表时也可以只注入：

```java
@SubscribeEvent
public static void onRegister(CollectibleRegisterEvent event) {
    event.builder().registerCollectible("other_relic",
            (stack, level, player) -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1)));
}
```

`registerCollectible` 提供多组重载：只给效果时使用默认 25 tick、普通稀有度、0–1 层；需要完整控制时使用七参版本（使用耗时、可重复使用、稀有度、层数、激活动画）。

## 运行时读写收藏品

`Collectibles` 是玩家级 Capability，服务端权威：写入必须走 `Collectibles` 的静态方法，内部会同步客户端。

```java
Item relic = MyCollectibles.SAMPLE_RELIC.get();

boolean used = Collectibles.isUsed(player, relic);
int layer = Collectibles.getLayerData(player).getLayer(relic);

Collectibles.addLayerAndSync(player, relic, 1);   // 改层数并同步
Collectibles.activateAndSync(player, relic);     // 直接激活（指令等场景）
Collectibles.removeAndSync(player, relic);       // 移除并同步

long endTick = Collectibles.getCooldownData(player).getCooldownEndTick(relic);
```

注意：

- 以上方法只能由服务端调用，客户端直接修改不会生效。
- 玩家处于旁观者模式时 `Collectibles.isEffectSuppressed` 返回 true，藏品效果全部停效。
- 服务端配置 `ban_advanced_collectibles` 为 true 时，`ADVANCED` 级藏品无法激活。

## 元素损伤

`EPManager` 是对外唯一入口，内部已处理空实体与 `empty` 类型短路。

| 方法 | 说明 |
|---|---|
| `hurtElemental(target, type, amount)` | 施加损伤 |
| `hurtElemental(target, type, attacker, amount)` | 带攻击者，触发带来源的结算与事件 |
| `healElemental(target, type, amount)` | 恢复损伤 |
| `healToFull(target, type)` | 恢复至上限 |
| `getValue` / `getMaxValue` | 当前值与上限 |
| `getRemainProgress` | 距离爆条的剩余进度（0–1） |
| `underBurst` / `isUnderBreak` | 是否处于爆条 / 破条状态 |
| `isImmune` / `setImmune` / `setAllImmune` / `setPermanentImmunity` | 免疫控制 |

损伤类型见 `AbstractEPCapability.EPType`：`NERVOUS`、`CORROSION`、`BURN`、`NECROSIS`、`FRENZY`，另有 `empty` 占位类型，传入占位类型时不产生效果。

### 自定义攻击与防御

- 实现 `ElementalAttacker`（实体或被攻击者任意装备栏物品）：提供损伤类型、累积速率与单次损伤值，多来源会累加。
- 实现 `ElementalDefender`（受击实体）：提供受击时的损伤类型与基础 / 总倍率，未实现时按神经损伤与 1.0 倍率处理。
- 实现 `ElementalAttackModifier` / `ElementalDefenseModifier`（通常由 `MobEffect` 实现）：在结算时修改倍率，防御侧仅在效果类型与受击方损伤类型一致时生效。

### 典型用法

```java
EPManager.hurtElemental(target, AbstractEPCapability.EPType.BURN, attacker, 200);
int value = EPManager.getValue(target, AbstractEPCapability.EPType.NERVOUS);
```

## 目标生命与护盾

```java
HealthCapability health = BabelCapability.getHealth(entity);   // 可能为 null
if (health != null) {
    double lives = health.getLives();
    double maxLives = health.getMaxLives();
    double shield = health.getShield();

    health.setShield(shield + 50);
    health.setLives(lives - 1);
    health.sync();   // 修改后同步客户端
}
```

`BabelCapability.getEP(living)` 返回 `EPCapability`，同样可能为 null。两者都由 Capability 系统自动挂到 `LivingEntity` 上并持久化，无需自行注册。

致命伤害的扣除顺序为护盾 → 目标生命 → 真实死亡，对应 `LifePointConsumeEvent.Type` 的 `SHIELD`、`LIVES`、`FAIL`。

## 难度

```java
NDifficultyLevel level = NDifficulty.difficultyLevel(world);   // 0 表示未启用
int scaled = NDifficulty.multiplier(world, 10, new float[]{1.0F, 1.2F, 1.5F, 2.0F});
```

- 数据源为游戏规则 `difficultyLevel`，取值 `0` 关闭、`1–18` 生效。
- 档位枚举 `NDifficultyTier` 划分：`EASY` 0–2、`NORMAL` 3–5、`HARD` 6–8、`EXPERT` 9–11、`PERILOUS` 12–18。
- `NDifficulty.acceptSynced` / `clearSynced` 由网络层使用，第三方无需调用。

## 掉落接入

- 收藏品掉落：监听 `CollectibleDropEvent`，`isGuaranteed()` 为 true 表示 boss 必掉且固定取高级藏品；`setChance(0)` 阻止掉落。
- 源石锭掉落：监听 `OriginiumIngotDropEvent`，倍率作用于基础数量 1，小数部分按概率进位（1.5 表示必掉 1 个且有 50% 再掉 1 个）。

## 端侧与线程

- 所有状态修改都在服务端完成，客户端只读取 Capability 用于渲染；不要在客户端调用 `*AndSync` 或写入 NBT。
- `BabelCapability.getEP` / `getHealth`、`Collectibles.getData` 都可能返回 null，访问前必须判空。
- Forge 事件默认在两侧都会触发，需要区分时检查 `event.getEntity().level().isClientSide`。
- 异步线程回调中操作实体必须切回服务端主线程（`server.execute(...)`），不要直接调用 `hurtElemental` 等方法。

## 常见扩展场景

| 场景 | 推荐做法 |
|---|---|
| 给实体加防御 / 法抗 | 通过 `AttributeModifier` 挂到 `babel:defense`、`babel:magic_resistance`，不要直接 `setBaseValue` 覆盖其他模组 |
| 自定义减伤或闪避 | 监听 `ResistDamageEvent`、`DodgeEvent`，在事件内修改生效值或处理闪避表现 |
| 自定义元素损伤来源 | 让物品或实体实现 `ElementalAttacker`，或调用 `hurtElemental` 时显式传入 `HurtType` |
| 药水增强 / 削弱损伤 | 让 `MobEffect` 实现 `ElementalAttackModifier` 或 `ElementalDefenseModifier`，效果类型需与损伤类型一致 |
| 发放收藏品 | 服务端调用 `Collectibles.activateAndSync` 或 `addLayerAndSync`，避免自行构造同步包 |
| 新增藏品获取途径 | 监听 `CollectibleDropEvent` 调整概率，或在自己的战利品表中直接写入藏品物品 |

## 调试

- 使用自带指令验证数值：`/ep`、`/lifepoint`、`/collectible`，可排除 HUD 与效果之间的干扰。
- 收藏品无效果时依次检查：玩家是否为旁观者、`ban_advanced_collectibles` 是否开启、藏品层数是否为 0、逻辑是否只在服务端执行。
- 服务端正常但客户端不显示时，先确认客户端已安装本模组，再检查是否遗漏 `sync()` 调用。
- 开发环境建议开启 debug 日志并订阅相关事件打印来源，正式发布前移除。

## 版本与兼容策略

- 依赖时使用 `fg.deobf` 与固定版本；需要放宽可写 `[1.2,1.3)` 这类范围，避免小版本间的行为差异。
- `api`、`collectible`、`elemental`、`capability`、`difficulty` 五个包内的公开类与事件名不会在小版本中删除；新增方法保持可选，已有方法签名不做破坏性变更。
- MixinExtras 由 jarJar 内嵌提供，依赖方不要重复打包，否则会出现重复类。
- 升级前先对照本指南的事件表检查是否有新增的取消语义或参数变化。

## 开发者 FAQ

**`getHealth` / `getEP` 返回 null？**

Capability 由实体加入世界时附加，刚创建尚未入世界、或实体已移除时可能为空，先判空再使用。

**收藏品效果只在单人正常，服务器失效？**

检查逻辑是否在客户端执行。服务端没装本模组时，`Collectibles` 数据根本不存在。

**数值改了但客户端显示没变？**

`HealthCapability`、`EPCapability` 的修改需要调用 `sync()`，收藏品需要走 `*AndSync` 静态方法。

**能不能直接改 `EPCapability` 内部状态？**

不推荐。绕过 `EPManager` 会跳过事件、倍率与同步逻辑，行为与原版不一致。

## 兼容说明

- 元素损伤、目标生命、护盾均为服务端结算，客户端只负责渲染，多人服务器下需客户端与服务端同时安装本模组。
- 本模组通过 jarJar 内嵌 MixinExtras，作为依赖引入时不会把你的 MixinExtras 打包进本体。
- Jade 为软依赖，JEI 仅编译期依赖，均不影响运行。
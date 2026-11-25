/**
 * 收藏品注册的对外契约：注册表入口 {@code CollectibleRegistration}、声明接口 {@code CollectibleRegistrar} 与参数 {@code CollectibleSpec}。
 * 构造阶段在自己的 mod 总线上安装声明回调，物品于 Forge 注册阶段创建。
 */
package com.susen36.babel.api.collectible;
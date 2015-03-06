/**
 * Copyright (c) Lambda Innovation, 2013-2015
 * 本作品版权由Lambda Innovation所有。
 * http://www.li-dev.cn/
 *
 * This project is open-source, and it is distributed under  
 * the terms of GNU General Public License. You can modify
 * and distribute freely as long as you follow the license.
 * 本项目是一个开源项目，且遵循GNU通用公共授权协议。
 * 在遵照该协议的情况下，您可以自由传播和修改。
 * http://www.gnu.org/licenses/gpl.html
 */
package cn.academy.ability.electro.skill;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import cn.academy.ability.electro.CatElectro;
import cn.academy.ability.electro.client.render.skill.RailgunPlaneEffect;
import cn.academy.ability.electro.entity.EntityRailgun;
import cn.academy.ability.electro.entity.fx.ChargeEffectS;
import cn.academy.api.ability.SkillBase;
import cn.academy.api.ctrl.RawEventHandler;
import cn.academy.api.ctrl.SkillState;
import cn.academy.api.ctrl.pattern.PatternDown;
import cn.academy.api.data.AbilityData;
import cn.academy.api.data.AbilityDataMain;
import cn.academy.api.event.ThrowCoinEvent;
import cn.academy.core.client.render.SkillRenderManager;
import cn.academy.core.ctrl.EventHandlerClient;
import cn.academy.misc.entity.EntityThrowingCoin;
import cn.annoreg.core.RegistrationClass;
import cn.annoreg.mc.RegEventHandler;
import cn.annoreg.mc.RegEventHandler.Bus;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * 传说中的超电磁炮~~！
 * TODO add other item support, better QTE
 * @author WeathFolD
 */
@RegistrationClass
@RegEventHandler(Bus.Forge)
public class SkillRailgun extends SkillBase {

	public SkillRailgun() {
		setLogo("electro/railgun.png");
		setName("em_railgun");
		setMaxLevel(6);
	}
	
	private static Map<EntityPlayer, EntityThrowingCoin> etcData = new HashMap();
	
	@Override
	public void initPattern(RawEventHandler reh) {
		reh.addPattern(new PatternDown() {
			@Override
			public SkillState createSkill(EntityPlayer player) {
				return new RailgunState(player);
			}
		}.setCooldown(2000)); //先意思意思，现在服务端和客户端的同步真心不能忍……
	}
	
	public static class RailgunState extends SkillState {

		public RailgunState(EntityPlayer player) {
			super(player);
		}
		
		protected void onStart() {
			AbilityData data = AbilityDataMain.getData(player);
			int slv = data.getSkillID(CatElectro.railgun), lv = data.getLevelID() + 1;
			
			System.out.println("onstart? " + player.worldObj.isRemote);
			
			EntityThrowingCoin etc = etcData.get(player);
			if(etc == null) {
				System.out.println("noooo? " + player.worldObj.isRemote);
				this.finishSkill(false);
				return;
			}
			
			if(player.worldObj.isRemote) {
				player.worldObj.spawnEntityInWorld(
					new ChargeEffectS.Strong(data.getPlayer(), 30, 4));
			}
			
			System.out.println("huh? " + etc.worldObj.isRemote);
			if(!etc.isDead) { 
				if(etc.getProgress() > 0.7) {
					float consume = 2200 - 15 * (slv * slv);
					if(!data.decreaseCP(consume, CatElectro.railgun)) {
						finishSkill(false);
						return;
					}
					
					if(!player.worldObj.isRemote) {
						player.worldObj.playSoundAtEntity(player, "academy:elec.railgun", 0.5f, 1.0f);
						player.worldObj.spawnEntityInWorld(new EntityRailgun(AbilityDataMain.getData(player)));
						etc.setDead();
					}
					System.out.println("fin in " + etc.worldObj.isRemote);
					finishSkill(true);
					return;
				} else {
					finishSkill(true);
					return;
				}
			}
			finishSkill(false);
		}
	}
	
	@SubscribeEvent
	public void throwCoin(ThrowCoinEvent event) {
		AbilityData data = AbilityDataMain.getData(event.entityPlayer);
		System.out.println("railgun render to1");
		
		EntityThrowingCoin id = etcData.get(event.entityPlayer);
		if(id != null && !id.isDead)
			return;
		
		if(data.getCategory() != CatElectro.INSTANCE || 
			!data.isSkillLearned(CatElectro.railgun) || !data.isActivated()) {
				return;
		}
		System.out.println("railgun render to2");
		if(data.getPlayer().worldObj.isRemote) {
			if(!EventHandlerClient.isSkillMapped(data.getSkillID(CatElectro.railgun)))
				return;
		}
		
		etcData.put(event.entityPlayer, event.coin);
		
		if(event.entityPlayer.worldObj.isRemote) {
			System.out.println("真的加了特技啦~~ " + event.coin.getEntityId());
			SkillRenderManager.addEffect(RailgunPlaneEffect.instance, 
					RailgunPlaneEffect.getAnimLength());
		}
	}
}

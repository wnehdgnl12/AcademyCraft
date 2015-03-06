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
package cn.academy.api.ability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import cn.academy.api.ctrl.RawEventHandler;
import cn.academy.api.ctrl.pattern.PatternHold;

public class SkillHoldTest extends SkillBase {

	public static class State extends PatternHold.State {

		public State(EntityPlayer player) {
			super(player);
		}

		@Override
		public void onStart() {
			if (!player.worldObj.isRemote) {
				player.addChatComponentMessage(new ChatComponentText("Hold Skill: Start."));
			}
		}

		@Override
		public boolean onFinish(boolean result) {
			if (!player.worldObj.isRemote) {
				player.addChatComponentMessage(new ChatComponentText("Hold Skill: Finish."));
			}
			return true;
		}

		@Override
		public void onHold() {
			if (!player.worldObj.isRemote) {
				player.addChatComponentMessage(new ChatComponentText("Hold Skill: Hold."));
			}
		}
		
	}
	
	@Override
	public void initPattern(RawEventHandler reh) {
		reh.addPattern(new PatternHold(30) {

			@Override
			public State createSkill(EntityPlayer player) {
				return new SkillHoldTest.State(player);
			}
			
		});
	}
	
}

/**
 * 
 */
package cn.academy.energy.msg;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cn.academy.api.energy.IWirelessNode;
import cn.annoreg.core.RegistrationClass;
import cn.annoreg.mc.RegMessageHandler;
import cn.annoreg.mc.RegMessageHandler.Side;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author WeathFolD
 *
 */
@RegistrationClass
public class MsgEnergyHeartbeat implements IMessage {
	
	int x, y, z;
	float energy;

	public MsgEnergyHeartbeat(IWirelessNode node) {
		TileEntity te = (TileEntity) node;
		x = te.xCoord;
		y = te.yCoord;
		z = te.zCoord;
		energy = (float) node.getEnergy();
	}
	
	public MsgEnergyHeartbeat() {}

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		energy = buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x).writeInt(y).writeInt(z).writeFloat(energy);
	}
	
	@RegMessageHandler(msg = MsgEnergyHeartbeat.class, side = Side.CLIENT)
	public static class Handler implements IMessageHandler<MsgEnergyHeartbeat, IMessage> {

		@Override
		@SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
		public IMessage onMessage(MsgEnergyHeartbeat msg, MessageContext ctx) {
			World world = Minecraft.getMinecraft().theWorld;
			TileEntity te = world.getTileEntity(msg.x, msg.y, msg.z);
			if(te instanceof IWirelessNode) {
				((IWirelessNode)te).setEnergy(msg.energy);
			}
			return null;
		}
		
	}

}

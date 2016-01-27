/**
 * 
 */
package cn.academy.crafting.block;

import cn.academy.core.block.TileReceiverBase;
import cn.academy.core.client.sound.ACSounds;
import cn.academy.core.client.sound.PositionedSound;
import cn.academy.crafting.api.MetalFormerRecipes;
import cn.academy.crafting.api.MetalFormerRecipes.RecipeObject;
import cn.academy.energy.IFConstants;
import cn.academy.support.EnergyItemHelper;
import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegTileEntity;
import cn.lambdalib.networkcall.RegNetworkCall;
import cn.lambdalib.networkcall.s11n.StorageOption.Data;
import cn.lambdalib.networkcall.s11n.StorageOption.Instance;
import cn.lambdalib.networkcall.s11n.StorageOption.RangedTarget;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * @author WeAthFolD
 */
@Registrant
@RegTileEntity
public class TileMetalFormer extends TileReceiverBase {
    
    public enum Mode { 
        PLATE, INCISE, ETCH, REFINE; 
        
        public final ResourceLocation texture;
        private Mode() {
            texture = new ResourceLocation(
                    "academy:textures/guis/mark/mark_former_" + 
                    this.toString().toLowerCase() + ".png");
        }
    }; 
    
    public static final int 
        SLOT_IN = 0,
        SLOT_OUT = 1,
        SLOT_BATTERY = 2;
    
    public static final int
        WORK_TICKS = 60;
    
    public static final double
        CONSUME_PER_TICK = 13.3;
    
    // Available in both sides.
    public Mode mode = Mode.PLATE;
    public RecipeObject current;
    
    public int workCounter;
    public int updateCounter;

    public TileMetalFormer() {
        super("metal_former", 3, 3000, IFConstants.LATENCY_MK1);
    }
    
    @Override
    public void updateEntity() {
        super.updateEntity();
        
        World world = getWorldObj();
        if(!world.isRemote) {
            if(current != null) {
                // Process recipe
                if(this.pullEnergy(CONSUME_PER_TICK) == CONSUME_PER_TICK && !isActionBlocked()) {
                    ++workCounter;
                    if(workCounter == WORK_TICKS) { // Finish the job.
                        ItemStack inputSlot = this.getStackInSlot(SLOT_IN);
                        ItemStack outputSlot = this.getStackInSlot(SLOT_OUT);
                        inputSlot.stackSize -= current.input.stackSize;
                        if(inputSlot.stackSize == 0)
                            this.setInventorySlotContents(SLOT_IN, null);
                        
                        if(outputSlot != null)
                            outputSlot.stackSize += current.output.stackSize;
                        else
                            this.setInventorySlotContents(SLOT_OUT, current.output.copy());
                        
                        current = null;
                        workCounter = 0;
                    }
                } else {
                    current = null;
                    workCounter = 0;
                }
            } else {
                if(++workCounter == 5) {
                    current = MetalFormerRecipes.INSTANCE.getRecipe(this.getStackInSlot(SLOT_IN), mode);
                    workCounter = 0;
                }
            }
            
            /* Process energy in/out */ {
                ItemStack stack = this.getStackInSlot(SLOT_BATTERY);
                if(stack != null && EnergyItemHelper.isSupported(stack)) {
                    double gain = EnergyItemHelper
                            .pull(stack, Math.min(getMaxEnergy() - getEnergy(), getBandwidth()), false);
                    this.injectEnergy(gain);
                }
            }
            
            if(++updateCounter == 10) {
                updateCounter = 0;
                sync();
            }
        } else {
            updateSounds();
        }
    }
    
    // Cycle the mode. should be only called in SERVER.
    public void cycleMode() {
        mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
        sync();
    }
    
    // SERVER only
    private void sync() {
        syncData(this, workCounter, current, mode);
    }
    
    private boolean isActionBlocked() {
        if(current == null) {
            return true;
        }
        
        ItemStack inputSlot = this.getStackInSlot(SLOT_IN), outputSlot = this.getStackInSlot(SLOT_OUT);
        return !(current.accepts(inputSlot, mode) && 
            (outputSlot == null || 
            (outputSlot.getItem() == current.output.getItem() && 
            outputSlot.getItemDamage() == current.output.getItemDamage() &&
            outputSlot.stackSize + current.output.stackSize <= outputSlot.getMaxStackSize())));
    }
    
    public boolean isWorkInProgress() {
        return current != null;
    }
    
    public double getWorkProgress() {
        return isWorkInProgress() ? (double) workCounter / WORK_TICKS : 0;
    }
    
    @RegNetworkCall(side = Side.CLIENT)
    private static void syncData(
            @RangedTarget(range = 12) TileMetalFormer target,
            @Data Integer counter, 
            @Instance(nullable = true) RecipeObject recipe,
            @Instance Mode mode) {
        target.workCounter = counter;
        target.current = recipe;
        target.mode = mode;
    }
    
    // --- CLIENT EFFECTS
    
    @SideOnly(Side.CLIENT)
    private PositionedSound sound;
    
    @SideOnly(Side.CLIENT)
    private void updateSounds() {
        if(sound != null && !isWorkInProgress()) {
            sound.stop();
            sound = null;
        } else if(sound == null && isWorkInProgress()) {
            sound = new PositionedSound(xCoord + .5, yCoord + 5., zCoord + .5, 
                    "machine.machine_work").setLoop().setVolume(0.6f);
            ACSounds.playClient(sound);
        }
    }

}

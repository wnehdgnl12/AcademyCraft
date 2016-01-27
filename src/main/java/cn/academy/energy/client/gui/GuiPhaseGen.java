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
package cn.academy.energy.client.gui;

import cn.academy.energy.block.ContainerPhaseGen;
import cn.academy.energy.block.TilePhaseGen;
import cn.lambdalib.cgui.gui.CGuiScreenContainer;
import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.WidgetContainer;
import cn.lambdalib.cgui.gui.component.ProgressBar;
import cn.lambdalib.cgui.gui.event.FrameEvent;
import cn.lambdalib.cgui.xml.CGUIDocument;
import cn.lambdalib.util.client.font.IFont.FontOption;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * @author WeAthFolD
 */
public class GuiPhaseGen extends CGuiScreenContainer {
    
    static final WidgetContainer document = CGUIDocument.panicRead(new ResourceLocation("academy:guis/phase_gen.xml"));

    public final TilePhaseGen tile;
    
    Widget main;
    
    public GuiPhaseGen(ContainerPhaseGen c) {
        super(c);
        tile = c.tile;
        init();
    }
    
    void init() {
        main = document.getWidget("main").copy();

        main.getWidget("prog_liquid").listen(FrameEvent.class, (w, e) -> {
            ProgressBar.get(w).progress = (double) tile.getLiquidAmount() / tile.getTankSize();
        });

        main.getWidget("prog_buffer").listen(FrameEvent.class, (w, e) -> {
            ProgressBar.get(w).progress = tile.getEnergy() / tile.bufferSize;
        });

        EnergyUIHelper.initNodeLinkButton(tile, main.getWidget("btn_link"));
        
        gui.addWidget(main);
    }
    
    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        GL11.glPushMatrix();
        GL11.glTranslated(-guiLeft, -guiTop, 0);
        
        Widget w = gui.getTopWidget(x, y);
        if(w != null) {
            String text = null;
            switch(w.getName()) {
            case "prog_liquid":
                text = tile.getLiquidAmount() + "/" + tile.getTankSize() + "mB";
                break;
            case "prog_buffer":
                text = String.format("%.1f/%.1fIF", tile.getEnergy(), tile.bufferSize);
                break;
            }
            
            if(text != null) {
                //int offsetX = -160, offsetY = -45;
                GL11.glEnable(GL11.GL_BLEND);
                EnergyUIHelper.drawTextBox(text, x + 5, y + 5, new FontOption(10));
            }
        }
        
        GL11.glPopMatrix();
    }

}

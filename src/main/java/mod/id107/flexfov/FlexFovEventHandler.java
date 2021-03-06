package mod.id107.flexfov;

import org.lwjgl.opengl.GL11;

import mod.id107.flexfov.gui.SettingsGui;
import mod.id107.flexfov.projection.Projection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FlexFovEventHandler {

	private static boolean renderHand;
	
	public FlexFovEventHandler() {
		Shader.createShaderProgram(Projection.getProjection());
	}
	
	@SubscribeEvent
	public void initGui(InitGuiEvent.Post e) {
		if (e.gui instanceof GuiOptions) {
			e.buttonList.add(new GuiButton(18107, e.gui.width / 2 - 155,
					e.gui.height / 6 + 15, 150, 20, SettingsGui.screenTitle));
		}
	}
	
	@SubscribeEvent
	public void actionPerformed(ActionPerformedEvent.Pre e) {
		if (e.gui instanceof GuiOptions && e.button.id == 18107) {
			e.gui.mc.gameSettings.saveOptions();
			e.gui.mc.displayGuiScreen(SettingsGui.getGui(e.gui));
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH) //render before other overlays
	public void onRenderTickStart(TickEvent.RenderTickEvent e) {
		if (Projection.active) {
			Minecraft mc = Minecraft.getMinecraft();
			if (!mc.skipRenderWorld && mc.theWorld != null) {
				Projection projection = Projection.getProjection();
				if (e.phase == TickEvent.Phase.START) {
					renderHand = mc.entityRenderer.renderHand;
					mc.entityRenderer.renderHand = Projection.getProjection().getRenderHand();
					BufferManager.setupFrame();
					projection.renderWorld(e.renderTickTime);
				} else {
					projection.saveRenderPass();
					projection.runShader(e.renderTickTime);
					projection.drawOverlay(e.renderTickTime);
					mc.entityRenderer.renderHand = renderHand;
				}
			}
		}
	}
	
	@SubscribeEvent
	public void cameraSetup(EntityViewRenderEvent.CameraSetup e) {
		if (Projection.active) {
			GL11.glTranslatef(0, 0, -0.05f);
			Projection.getProjection().onCameraSetup();
		}
	}
	
	/*@SubscribeEvent TODO
	public void getFOVModifier(EntityViewRenderEvent.FOVModifier e) {
		if (Projection.active) {
			e.setFOV(Projection.getProjection().getPassFOV(e.getFOV()));
		}
	}*/
	
	@Optional.Method(modid="replaymod")
	@SubscribeEvent
	public void replayModRenderPre(com.replaymod.render.events.ReplayRenderEvent.Pre e) {
		Log.info("Disabling Flex FOV");
		Projection.active = false;
	}
	
	@Optional.Method(modid="replaymod")
	@SubscribeEvent
	public void replayModRenderPost(com.replaymod.render.events.ReplayRenderEvent.Post e) {
		Log.info("Enabling Flex FOV");
		Projection.active = true;
	}
}

package com.xyazh.kanake.events;


import com.xyazh.kanake.Kanake;
import com.xyazh.kanake.libs.weaponlib.shader.jim.Shader;
import com.xyazh.kanake.libs.weaponlib.shader.jim.ShaderManager;
import com.xyazh.kanake.particle.simple.SimpleParticle;
import com.xyazh.kanake.render.FramebufferExample;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

@Mod.EventBusSubscriber(modid = Kanake.MODID)
@SideOnly(Side.CLIENT)
public class RenderEvent {
    public static final ArrayList<Runnable> POST_RENDER_TASKS = new ArrayList<>();
    public static final ArrayDeque<SimpleParticle> PARTICLES = new ArrayDeque<>();
    public static FramebufferExample SCREEN_FBO = null;
    public static FramebufferExample EFFECT_FBO = null;
    public static FramebufferExample LIGHT_FBO = null;
    public static FramebufferExample PONG_FBO = null;
    public static Shader EFFECT_SHADER = null;
    public static Shader WINDOW_SHADER = null;
    public static Shader PONG_SHADER = null;
    public static Minecraft MC = null;

    @SubscribeEvent
    public static void onRenderInit(TextureStitchEvent.Post event) {
        SCREEN_FBO = new FramebufferExample(true);
        EFFECT_FBO = new FramebufferExample(true);
        LIGHT_FBO = new FramebufferExample(false, 0.5f, GL11.GL_LINEAR);
        PONG_FBO = new FramebufferExample(false, 0.25f, GL11.GL_LINEAR);
        EFFECT_SHADER = ShaderManager.loadVMWShader("effect");
        WINDOW_SHADER = ShaderManager.loadVMWShader("window");
        PONG_SHADER = ShaderManager.loadVMWShader("pong");
        MC = Minecraft.getMinecraft();
    }

    @SubscribeEvent
    public static void onTickWorld(TickEvent.ClientTickEvent event){
        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }
        if (MC.isGamePaused())
        {
            return;
        }
        if (PARTICLES.isEmpty())
        {
            return;
        }
        Iterator<SimpleParticle> iterator = PARTICLES.iterator();
        while (iterator.hasNext())
        {
            SimpleParticle particle = iterator.next();
            try {
                particle.onUpdate();
            }catch (Exception e){
                iterator.remove();
            }
            if (!particle.isAlive())
            {
                iterator.remove();
            }
        }
    }

    public static void renderParticles(float partialTicks) {
        float f = ActiveRenderInfo.getRotationX();
        float f1 = ActiveRenderInfo.getRotationZ();
        float f2 = ActiveRenderInfo.getRotationYZ();
        float f3 = ActiveRenderInfo.getRotationXY();
        float f4 = ActiveRenderInfo.getRotationXZ();
        EntityPlayerSP entityIn = Minecraft.getMinecraft().player;
        SimpleParticle.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        SimpleParticle.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        SimpleParticle.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (SimpleParticle particle : PARTICLES){
            particle.renderParticle(buffer, entityIn, partialTicks,  f, f4, f1, f2, f3);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(516, 0.003921569F);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        tessellator.draw();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
    }

    public static void addPostRenderTask(Runnable task) {
        POST_RENDER_TASKS.add(task);
    }

    public static void foreachPostRenderTask(boolean clear) {
        for (Runnable task : POST_RENDER_TASKS) {
            try {
                task.run();
            } catch (Exception e) {
                Kanake.logger.error("Error in post render task: " + task.toString());
                e.printStackTrace();
            }
        }
        if (clear) {
            POST_RENDER_TASKS.clear();
        }
    }

    public static void drawWindowRect() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(-1, +1, 0).tex(0, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.pos(+1, +1, 0).tex(1, 1).color(255, 255, 255, 255).endVertex();
        bufferbuilder.pos(+1, -1, 0).tex(1, 0).color(255, 255, 255, 255).endVertex();
        bufferbuilder.pos(-1, -1, 0).tex(0, 0).color(255, 255, 255, 255).endVertex();
        tessellator.draw();
    }

    @SubscribeEvent
    public static void onWorldRendered(RenderWorldLastEvent event) {
        int activeTextureUnit = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);

        //获取屏幕帧缓冲，绑定到纹理单元0
        RenderEvent.foreachPostRenderTask(false);
        RenderEvent.renderParticles( event.getPartialTicks());
        SCREEN_FBO.copyBindBuffer();
        //再渲染到单独帧缓冲，用于求取单独能看见的光源
        EFFECT_FBO.renderToFramebufferStart();
        RenderEvent.foreachPostRenderTask(true);
        RenderEvent.renderParticles(event.getPartialTicks());
        EFFECT_FBO.renderToFramebufferEnd();

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.enableTexture2D();
        SCREEN_FBO.bindFramebufferTexture();
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        GlStateManager.enableTexture2D();
        EFFECT_FBO.bindFramebufferTexture();

        GlStateManager.disableCull();
        EFFECT_SHADER.use();
        EFFECT_SHADER.uniform1i("tex0", 0);
        EFFECT_SHADER.uniform1i("tex1", 1);

        LIGHT_FBO.renderToFramebufferStart();
        GlStateManager.viewport(0, 0, LIGHT_FBO.realWidth,LIGHT_FBO.realHeight);
        drawWindowRect();
        LIGHT_FBO.renderToFramebufferEnd();
        EFFECT_SHADER.release();
        GlStateManager.viewport(0, 0, LIGHT_FBO.width,LIGHT_FBO.height);

        FramebufferExample fbo = fboPingPong(LIGHT_FBO);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        fbo.bindFramebufferTexture();
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        SCREEN_FBO.bindFramebufferTexture();
        WINDOW_SHADER.use();
        WINDOW_SHADER.uniform1i("tex0", 0);
        WINDOW_SHADER.uniform1i("tex1", 1);
        drawWindowRect();
        WINDOW_SHADER.release();

        GlStateManager.enableCull();
        resetActiveTextures();
        GlStateManager.setActiveTexture(activeTextureUnit);
    }

    public static FramebufferExample fboPingPong(FramebufferExample fboPing){
        FramebufferExample fboPong = PONG_FBO;
        FramebufferExample[] fbo = {fboPing, fboPong};
        PONG_SHADER.use();
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        PONG_SHADER.uniform1i("tex0", 0);
        FramebufferExample result = fboPing;
        for(int i = 0;i < 10;i++){
            int owner = i % 2;
            int target = 1 - owner;
            fbo[owner].bindFramebufferTexture();
            fbo[target].renderToFramebufferStart();
            PONG_SHADER.uniform2f("wh", fbo[target].realWidth,fbo[target].realHeight);
            GlStateManager.viewport(0, 0, fbo[target].realWidth,fbo[target].realHeight);
            drawWindowRect();
            fbo[target].renderToFramebufferEnd();
            result = fbo[target];
        }
        PONG_SHADER.release();
        GlStateManager.viewport(0, 0, result.width,result.height);
        return result;
    }

    private static void resetActiveTextures() {
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(0);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(0);
    }
}

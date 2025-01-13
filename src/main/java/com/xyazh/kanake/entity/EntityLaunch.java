package com.xyazh.kanake.entity;

import com.xyazh.kanake.particle.ModParticles;
import com.xyazh.kanake.particle.simple.SimpleParticle;
import com.xyazh.kanake.particle.simple.particles.MagicParticle;
import com.xyazh.kanake.particle.simple.particles.MagicParticle1;
import com.xyazh.kanake.particle.simple.particles.TestParticle2;
import com.xyazh.kanake.util.Vec3d;
import io.netty.buffer.ByteBuf;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.UUID;

public class EntityLaunch extends EntityShoot{
    public EntityLaunch(World worldIn) {
        super(worldIn);
        this.livingMaxAge = 1200;
        this.speed = 0.7;
    }

    @Override
    public void entityInit() {
    }

    @Override
    protected boolean canFitPassenger(@Nonnull Entity passenger) {
        return true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if(!this.hasNoGravity()){
            dy += 0.0012;
        }
        if (world.isRemote) {
            for (int i = 0; i <= 10; i++) {
                SimpleParticle particle1 = new MagicParticle(world, posX, posY, posZ, 0, 0, 0);
                SimpleParticle particle2 = new MagicParticle1(world, posX, posY, posZ, 0, 0, 0);
            }
        }

        if(this.collided){
            this.setDeadParticle();
            this.setDead();
        }
    }

    protected void setDeadParticle(){
        if (world.isRemote) {
            for (int i = 0; i <= 100; i++) {
                SimpleParticle particle = new TestParticle2(world, posX, posY, posZ, 0, 0, 0);
            }
        }
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        this.livingMaxAge = buffer.readInt();
        this.livingAge = buffer.readInt();
        this.forward = new Vec3d(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()
        );
        this.speed = buffer.readDouble();
        this.dy = buffer.readDouble();
        boolean hasPlayerShooter = buffer.readBoolean();
        if(hasPlayerShooter){
            long highBits = buffer.readLong();
            long lowBits = buffer.readLong();
            UUID id = new UUID(highBits, lowBits);
            this.shootingEntity = this.world.getPlayerEntityByUUID(id);
            if(this.shootingEntity != null){
                this.shootingEntity.startRiding(this);
            }
        }
    }
}

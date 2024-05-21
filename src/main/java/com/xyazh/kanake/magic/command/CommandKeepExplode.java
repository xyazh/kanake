package com.xyazh.kanake.magic.command;

import com.xyazh.kanake.entity.EntityEmptyMagic;
import net.minecraft.entity.Entity;

public class CommandKeepExplode extends StaticCommand{
    public CommandKeepExplode(int order, boolean need_sync) {
        super(order, need_sync);
    }

    @Override
    public void execute(Entity entity) {
        if (entity instanceof EntityEmptyMagic) {
            EntityEmptyMagic emptyMagic = (EntityEmptyMagic) entity;
            emptyMagic.keepExplode = true;
        }
    }
}

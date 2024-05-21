package com.xyazh.kanake.world.tree;

import com.xyazh.kanake.util.Vec3d;
import net.minecraft.util.math.ChunkPos;

import java.util.*;

public class BigTreeGenerateTemplate {
    protected final float PI = 3.1415926f;
    protected final Random rand;
    protected Vec3d rootPos;
    protected final long seed;
    public HashMap<ChunkPos,HashSet<Branches>> chunks = new HashMap<>();
    protected static final LinkedHashMap<Long, BigTreeGenerateTemplate> TEMPLATES = new LinkedHashMap<Long, BigTreeGenerateTemplate>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, BigTreeGenerateTemplate> eldest) {
            return size() > 10;
        }
    };

    public static BigTreeGenerateTemplate getTemplate(long seed, Vec3d rootPos,float pitch,float yaw) {
        return TEMPLATES.computeIfAbsent(seed, k -> new BigTreeGenerateTemplate(seed, rootPos, pitch, yaw));
    }

    public BigTreeGenerateTemplate(long seed, Vec3d rootPos,float pitch,float yaw) {
        this.seed = seed;
        this.rand = new Random(seed);
        this.rootPos = rootPos;
        this.drawBranch(rootPos, 4, 108, pitch, yaw, 5);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BigTreeGenerateTemplate that = (BigTreeGenerateTemplate) o;
        return seed == that.seed && rootPos.equals(that.rootPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootPos, seed);
    }

    protected void drawBranch(Vec3d v0, float radius, float length, float pitch, float yaw, int maxDepth) {
        if (maxDepth <= 0) {
            return;
        }
        if (radius < 2 && this.rand.nextFloat() < 0.3) {
            return;
        }
        if (radius < 0.5) {
            return;
        }
        if (v0.y > 255) {
            return;
        }
        Vec3d v1 = Vec3d.fromPitchYaw(pitch, yaw);
        v1.mul(length);
        v1.add(v0);
        Branches branches = new Branches(v0, v1, radius);
        this.calculateChunksForBranch(branches);
        int count = this.rand.nextInt(2) + 2;
        for (int i = 0; i < count; i++) {
            this.drawBranch(v1, radius * (0.7f + this.rand.nextFloat() / 10), length * (0.5f + this.rand.nextFloat() / 2.5f), pitch - this.rand.nextFloat() * 45, this.rand.nextFloat() * 360, maxDepth - 1);
        }
    }

    public boolean containChunk(int x, int z) {
        return this.chunks.containsKey(new ChunkPos(x, z));
    }

    public void calculateChunksForBranch(Branches branch) {
        Vec3d start = branch.startPos;
        Vec3d end = branch.endPos;

        int startX = (int) Math.floor(start.x) >> 4;
        int startZ = (int) Math.floor(start.z) >> 4;
        int endX = (int) Math.floor(end.x) >> 4;
        int endZ = (int) Math.floor(end.z) >> 4;

        int minX = Math.min(startX, endX);
        int maxX = Math.max(startX, endX);
        int minZ = Math.min(startZ, endZ);
        int maxZ = Math.max(startZ, endZ);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                ChunkPos pos = new ChunkPos(x, z);
                if (!this.chunks.containsKey(pos)){
                    this.chunks.put(pos, new HashSet<>());
                }
                this.chunks.get(pos).add(branch);
            }
        }
    }

    public boolean contain(double x,double y,double z) {
        HashSet<Branches> branches = this.chunks.get(new ChunkPos((int) Math.floor(x) >> 4, (int) Math.floor(z) >> 4));
        if(branches == null){
            return false;
        }
        for (Branches b : branches) {
            if (b.contain(x,y,z)) {
                return true;
            }
        }
        return false;
    }
}

package benjas333.simplemeasure.client;

import net.minecraft.world.phys.Vec3;

public class BlockArea {
    public final int x;
    public final int y;
    public final int z;

    public BlockArea(Vec3 pos1, Vec3 pos2) {
        x = Math.abs((int) pos2.x - (int) pos1.x) + 1;
        y = Math.abs((int) pos2.y - (int) pos1.y) + 1;
        z = Math.abs((int) pos2.z - (int) pos1.z) + 1;
    }

    public int getVolume() {
        return x * y * z;
    }
}

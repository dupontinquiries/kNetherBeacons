package net.fabricmc.knb.helper;

import net.minecraft.entity.Entity;

public class VelocityHelper {

    public static double[] getDirectionalVelocities(Entity e1, Entity e2) {
        double _x, _y, _z;
        _x = e1.getPos().getX() - e2.getPos().getX();
        _y = e1.getPos().getY() - e2.getPos().getY();
        _z = e1.getPos().getZ() - e2.getPos().getZ();
        return new double[]{_x, _y, _z};
    }

}

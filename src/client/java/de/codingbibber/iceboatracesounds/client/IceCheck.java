package de.codingbibber.iceboatracesounds.client;

import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

/** Eis-Erkennung unter Booten – eine Stelle für Client-Logik und Mixin. */
public final class IceCheck {

    private IceCheck() {
    }

    public static boolean isBoatOnIce(ClientWorld world, Entity boat) {
        if (world == null || boat == null) {
            return false;
        }
        BlockPos below = boat.getBlockPos().down();
        var state = world.getBlockState(below);
        return state.isOf(Blocks.ICE)
                || state.isOf(Blocks.PACKED_ICE)
                || state.isOf(Blocks.BLUE_ICE)
                || state.isOf(Blocks.FROSTED_ICE);
    }
}

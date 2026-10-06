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
        return classify(world, boat) != Surface.OTHER;
    }

    /** Differenziert die Eis-Sorte für den Klangcharakter (ändert nichts am Block). */
    public static Surface classify(ClientWorld world, Entity boat) {
        if (world == null || boat == null) {
            return Surface.OTHER;
        }
        BlockPos below = boat.getBlockPos().down();
        var state = world.getBlockState(below);
        if (state.isOf(Blocks.BLUE_ICE)) {
            return Surface.BLUE;
        }
        if (state.isOf(Blocks.PACKED_ICE)) {
            return Surface.PACKED;
        }
        if (state.isOf(Blocks.FROSTED_ICE)) {
            return Surface.FROSTED;
        }
        if (state.isOf(Blocks.ICE)) {
            return Surface.ICE;
        }
        return Surface.OTHER;
    }
}

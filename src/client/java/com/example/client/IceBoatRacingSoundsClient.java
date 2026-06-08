package com.example.client;

import com.example.ModSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.sound.SoundCategory;

public class IceBoatRacingSoundsClient implements ClientModInitializer {

	private boolean wasInBoat = false;

	private EngineLoopSound engineSound;

	@Override
	public void onInitializeClient() {

		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if (client.player == null || client.world == null) return;

			boolean inBoat = client.player.getVehicle() instanceof BoatEntity;

			// ✔ START
			if (inBoat && !wasInBoat) {

				client.world.playSound(
						client.player,
						client.player.getBlockPos(),
						ModSounds.ENGINE_START,
						SoundCategory.PLAYERS,
						1.0f,
						1.0f
				);

				engineSound = new EngineLoopSound(client);
				client.getSoundManager().play(engineSound);
			}

			// ✔ STOP
			if (!inBoat && wasInBoat) {

				client.getSoundManager().stop(engineSound);
				engineSound = null;

				client.world.playSound(
						client.player,
						client.player.getBlockPos(),
						ModSounds.ENGINE_TURNOFF,
						SoundCategory.PLAYERS,
						1.0f,
						1.0f
				);
			}

			wasInBoat = inBoat;
		});
	}
}
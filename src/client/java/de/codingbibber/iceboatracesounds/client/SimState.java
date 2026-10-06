package de.codingbibber.iceboatracesounds.client;

/**
 * Geteilter Simulationszustand für alle Audio-Module. Alle Werte sind rein
 * intern und dienen ausschließlich dem Sounddesign – sie fließen niemals in
 * Minecraft-Physik, -Steuerung oder -Gameplay zurück (reine Beobachtung der
 * Vanilla-Bewegung).
 */
public final class SimState {

    // Bewegung (Beobachtung, m/s bzw. m/s^2)
    public float speedMs;
    public float prevSpeedMs;
    public float speedDrop;
    public float accel;
    public float vyMs;

    // Gas
    public boolean gas;
    public boolean wasGas;
    public float throttleRev;

    // Motor-Simulation
    public float load01;
    public float launchBoost;
    public int gear = 1;
    public float inGear;
    public float rpm01;
    public float rpmAdd;
    public float shiftDip;
    public float revBlip;
    public boolean limiterActive;
    public float limiterCut = 1.0f;

    // Drift / Untergrund / Umgebung
    public float slide01;
    public float lateralMs;
    public Surface surface = Surface.OTHER;
    public float boatPitch = 1.0f;
    public float openness = 1.0f;

    // Weltzustand
    public boolean grounded;
    public boolean wasGrounded = true;

    // Ducking: Motor tritt bei Transienten kurz zurück (1.0 = voll).
    public float duck = 1.0f;

    public int ticks;
}

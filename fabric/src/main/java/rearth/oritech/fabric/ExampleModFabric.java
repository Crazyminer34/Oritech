package rearth.oritech.fabric;

import net.fabricmc.api.ModInitializer;
import rearth.oritech.Oritech;

public final class ExampleModFabric implements ModInitializer {
    @Override
    public void onInitialize() {

        // Run our common setup.
        Oritech.runAllRegistries();
        Oritech.initialize();
    }
}

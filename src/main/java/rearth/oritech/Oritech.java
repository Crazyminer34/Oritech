package rearth.oritech;

import com.mojang.serialization.Codec;
import earth.terrarium.common_storage_lib.CommonStorageLib;
import earth.terrarium.common_storage_lib.data.DataManager;
import earth.terrarium.common_storage_lib.data.DataManagerRegistry;
import io.wispforest.owo.registration.reflect.FieldRegistrationHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rearth.oritech.block.blocks.pipes.EnergyPipeBlock;
import rearth.oritech.block.blocks.pipes.FluidPipeBlock;
import rearth.oritech.block.blocks.pipes.ItemPipeBlock;
import rearth.oritech.block.blocks.pipes.SuperConductorBlock;
import rearth.oritech.block.entity.machines.accelerator.AcceleratorParticleLogic;
import rearth.oritech.block.entity.pipes.GenericPipeInterfaceEntity;
import rearth.oritech.client.init.ModScreens;
import rearth.oritech.client.init.ParticleContent;
import rearth.oritech.init.*;
import rearth.oritech.init.recipes.RecipeContent;
import rearth.oritech.init.world.FeatureContent;
import rearth.oritech.network.NetworkContent;

public class Oritech implements ModInitializer {
    
    public static final String MOD_ID = "oritech";
    public static final Logger LOGGER = LoggerFactory.getLogger("oritech");
    public static final OritechConfig CONFIG = OritechConfig.createAndLoad();
    
    public static final DataManagerRegistry DATA_REGISTRY = new DataManagerRegistry(MOD_ID);
    public static final DataManager<Long> ENERGY_CONTENT = DATA_REGISTRY.builder(() -> 0L).serialize(Codec.LONG).networkSerializer(PacketCodecs.VAR_LONG).withDataComponent().copyOnDeath().buildAndRegister("energy");
    
    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
    
    @Override
    public void onInitialize() {
        
        // this shouldn't be needed, yet here we are
        CommonStorageLib.init();
        
        LOGGER.info("Begin Oritech initialization");
        
        Oritech.LOGGER.debug("registering fluids");
        FieldRegistrationHandler.register(FluidContent.class, MOD_ID, false);
        Oritech.LOGGER.debug("registering items");
        FieldRegistrationHandler.register(ItemContent.class, MOD_ID, false);
        Oritech.LOGGER.debug("registering blocks");
        FieldRegistrationHandler.register(BlockContent.class, MOD_ID, false);
        Oritech.LOGGER.debug("registering block entities");
        FieldRegistrationHandler.register(BlockEntitiesContent.class, MOD_ID, false);
        Oritech.LOGGER.debug("registering screen handlers");
        FieldRegistrationHandler.register(ModScreens.class, Oritech.MOD_ID, false);
        Oritech.LOGGER.debug("registering sounds");
        FieldRegistrationHandler.register(SoundContent.class, Oritech.MOD_ID, false);
        Oritech.LOGGER.debug("registering others...");
        FieldRegistrationHandler.register(ToolsContent.class, MOD_ID, false);
        ComponentContent.init();
        ToolsContent.registerEventHandlers();
        ItemGroups.registerItemGroup();
        RecipeContent.initialize();
        NetworkContent.registerChannels();
        ParticleContent.registerParticles();
        FeatureContent.initialize();
        LootContent.init();
        
        DATA_REGISTRY.init();
        
        // for pipe data
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        
        // for particle collisions
        ServerTickEvents.END_SERVER_TICK.register(elem -> AcceleratorParticleLogic.onTickEnd());
    }
    
    private void onServerStarted(MinecraftServer minecraftServer) {
        minecraftServer.getWorlds().forEach(world -> {
            if (world.isClient) return;
            
            var regKey = world.getRegistryKey().getValue();
            
            var dataId = "energy_" + regKey.getNamespace() + "_" + regKey.getPath();
            var result = world.getPersistentStateManager().getOrCreate(GenericPipeInterfaceEntity.PipeNetworkData.TYPE, dataId);
            EnergyPipeBlock.ENERGY_PIPE_DATA.put(regKey, result);
            
            var fluidDataId = "fluid_" + regKey.getNamespace() + "_" + regKey.getPath();
            var fluidResult = world.getPersistentStateManager().getOrCreate(GenericPipeInterfaceEntity.PipeNetworkData.TYPE, fluidDataId);
            FluidPipeBlock.FLUID_PIPE_DATA.put(regKey, fluidResult);
            
            var itemDataId = "item_" + regKey.getNamespace() + "_" + regKey.getPath();
            var itemResult = world.getPersistentStateManager().getOrCreate(GenericPipeInterfaceEntity.PipeNetworkData.TYPE, itemDataId);
            ItemPipeBlock.ITEM_PIPE_DATA.put(regKey, itemResult);
            
            var superConductorDataId = "superconductor_" + regKey.getNamespace() + "_" + regKey.getPath();
            var superConductorResult = world.getPersistentStateManager().getOrCreate(GenericPipeInterfaceEntity.PipeNetworkData.TYPE, superConductorDataId);
            SuperConductorBlock.SUPERCONDUCTOR_DATA.put(regKey, superConductorResult);
        });
    }
}
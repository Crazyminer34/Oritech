package rearth.oritech.block.blocks.machines.generators;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import rearth.oritech.block.base.block.PassiveGeneratorBlock;
import rearth.oritech.block.entity.machines.generators.BigSolarPanelEntity;
import rearth.oritech.network.NetworkContent;
import rearth.oritech.util.MultiblockMachineController;

import static rearth.oritech.block.base.block.MultiblockMachine.ASSEMBLED;

public class BigSolarPanelBlock extends PassiveGeneratorBlock {
    
    public final int productionRate;
    
    public BigSolarPanelBlock(Settings settings, int productionRate) {
        super(settings);
        this.productionRate = productionRate;
        setDefaultState(getDefaultState().with(ASSEMBLED, false));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ASSEMBLED);
    }
    
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BigSolarPanelEntity(pos, state);
    }
    
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        
        if (!world.isClient) {
            
            var entity = world.getBlockEntity(pos);
            if (!(entity instanceof BigSolarPanelEntity solarPanel)) {
                return ActionResult.SUCCESS;
            }
            
            var wasAssembled = state.get(ASSEMBLED);
            var isAssembled = solarPanel.initMultiblock(state);
            
            // first time created
            if (isAssembled && !wasAssembled) {
                NetworkContent.MACHINE_CHANNEL.serverHandle(entity).send(new NetworkContent.MachineSetupEventPacket(pos));
                return ActionResult.SUCCESS;
            }
            
            if (!isAssembled) {
                player.sendMessage(Text.literal("Machine is not assembled. Please add missing core blocks"));
            } else {
                solarPanel.sendInfoMessageToPlayer(player);
            }
            return ActionResult.SUCCESS;
            
        }
        
        return ActionResult.SUCCESS;
    }
    
    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        
        if (!world.isClient() && state.get(ASSEMBLED)) {
            
            var entity = world.getBlockEntity(pos);
            if (entity instanceof MultiblockMachineController machineEntity) {
                machineEntity.onControllerBroken();
            }
        }
        
        return super.onBreak(world, pos, state, player);
    }
    
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }
}

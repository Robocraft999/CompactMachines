package dev.compactmods.machines.datagen;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.tunnel.TunnelWallBlock;
import dev.compactmods.machines.tunnel.Tunnels;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class StateGenerator extends BlockStateProvider {
    public StateGenerator(PackOutput packOutput, ExistingFileHelper exFileHelper) {
        super(packOutput, Constants.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Wall block model
        var wall = models().cubeAll("block/wall", modLoc("block/wall"));
        simpleBlock(Rooms.Blocks.SOLID_WALL.get(), wall);
        simpleBlock(Rooms.Blocks.BREAKABLE_WALL.get(), wall);

        final var m = models()
                .withExistingParent("block/machine/machine", mcLoc("block/block"))
                .texture("border", modLoc("block/machine/border"))
                .texture("tint", modLoc("block/machine/tint"))
                .texture("overlay", modLoc("block/machine/overlay"))
                .renderType(mcLoc("cutout"))
                .element()
                .allFaces((dir, face) ->
                        face.texture("#border")
                                .uvs(0, 0, 16, 16)
                                .cullface(dir)
                                .end()
                )
                .end()
                .element()
                .allFaces((dir, face) ->
                        face.texture("#tint")
                                .emissivity(2, 0)
                                .uvs(0, 0, 16, 16)
                                .cullface(dir)
                                .tintindex(0)
                                .end()
                )
                .end()
                .element()
                .allFaces((dir, face) ->
                        face.texture("#overlay")
                                .uvs(0, 0, 16, 16)
                                .cullface(dir)
                                .tintindex(1)
                                .end()
                )
                .end();

        simpleBlock(Machines.Blocks.UNBOUND_MACHINE.get(), ConfiguredModel.builder()
                .modelFile(m)
                .build());

        simpleBlock(Machines.Blocks.BOUND_MACHINE.get(), ConfiguredModel.builder()
                .modelFile(m)
                .build());

        // Machine models
        /*for(RoomSize size : RoomSize.values()) {
            String sizeName = size.getName();

            var mod = models()
                    .cubeAll("block/machine/machine_" + sizeName, modLoc("block/machine/machine_" + sizeName));

            simpleBlock(CompactMachineBlock.getBySize(size), ConfiguredModel.builder()
                    .modelFile(mod)
                    .build());
        }*/

        //Tunnels
        Block block = Tunnels.BLOCK_TUNNEL_WALL.get();

        generateTunnelBaseModel();

        for (Direction dir : Direction.values()) {

            String typedTunnelDirectional = "tunnels/" + dir.getSerializedName();
            models()
                    .withExistingParent(typedTunnelDirectional, modLoc("tunnels/base"))
                    .texture("wall", modLoc("block/" + typedTunnelDirectional))
                    .renderType(mcLoc("cutout")); // NamedRenderTypeManager


            /*
             If we ever do one-side has the texture again
             int x = dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0;
             int y = dir.getAxis().isVertical() ? 0 : (((int) dir.getHorizontalAngle()) + 180) % 360;
            */

            getVariantBuilder(block)
                    .partialState()
                    .with(TunnelWallBlock.CONNECTED_SIDE, dir)
                    .setModels(
                            ConfiguredModel.builder()
                                    .modelFile(models().getExistingFile(modLoc(typedTunnelDirectional)))
                                    .build()
                    );

        }
    }

    private void generateTunnelBaseModel() {
        BlockModelBuilder base = models()
                .withExistingParent("tunnels/base", new ResourceLocation("minecraft", "block/block"))
                .texture("tunnel", modLoc("block/tunnels/tunnel"))
                .texture("indicator", modLoc("block/tunnels/indicator"));

        addFullCubeWithTexture(base, "#wall", -1);
        addFullCubeWithTexture(base, "#tunnel", 0);
        addFullCubeWithTexture(base, "#indicator", 1);
    }

    private void addFullCubeWithTexture(BlockModelBuilder base, String texture, int tintIndex) {
        base.element()
                .from(0, 0, 0)
                .to(16, 16, 16)
                .allFaces((direction, faceBuilder) -> {
                    ModelBuilder<BlockModelBuilder>.ElementBuilder.FaceBuilder f = faceBuilder
                            .uvs(0, 0, 16, 16)
                            .cullface(direction);

                    if (tintIndex > -1)
                        f.tintindex(tintIndex);

                    f.texture(texture)

                            .end();
                })
                .end();
    }
}

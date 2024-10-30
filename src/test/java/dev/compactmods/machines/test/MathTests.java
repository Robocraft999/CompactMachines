package dev.compactmods.machines.test;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.util.BlockSpaceUtil;
import dev.compactmods.machines.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.HashMap;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class MathTests {

    @GameTest(template = "empty_1x1", batch = TestBatches.MATH)
    public static void positionGeneratorWorksCorrectly(final GameTestHelper test) {
        // Our generation works in a counter-clockwise spiral, starting at 0,0
        /*
         *    6  5  4
         *    7  0  3
         *    8  1  2
         */

        HashMap<Integer, ChunkPos> tests = new HashMap<>();
        tests.put(0, new ChunkPos(0, 0));
        tests.put(1, new ChunkPos(0, -64));
        tests.put(2, new ChunkPos(64, -64));
        tests.put(3, new ChunkPos(64, 0));
        tests.put(4, new ChunkPos(64, 64));
        tests.put(5, new ChunkPos(0, 64));
        tests.put(6, new ChunkPos(-64, 64));
        tests.put(7, new ChunkPos(-64, 0));
        tests.put(8, new ChunkPos(-64, -64));

        tests.forEach((id, expectedChunk) -> {
            Vec3i byIndex = MathUtil.getRegionPositionByIndex(id);
            BlockPos finalPos = MathUtil.getCenterWithY(byIndex, 0);

            ChunkPos calculatedChunk = new ChunkPos(finalPos);

            String error = String.format("Generation did not match for %s.", id);
            if(!expectedChunk.equals(calculatedChunk))
                test.fail(error);
        });

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.MATH)
    public static void wallsAreCalculatedCorrectly(final GameTestHelper test){
        AABB outerBounds = new AABB(0.5, 0, 0.5, 15.5, 15, 15.5);
        /*AABB up = BlockSpaceUtil.getWallBounds(outerBounds, Direction.UP);
        if (!up.equals(new AABB(1, 14, 1, 16, 15, 16)))
            test.fail(String.format("Wall bound (%s) did not match for direction %s.", up, Direction.UP));
        AABB down = BlockSpaceUtil.getWallBounds(outerBounds, Direction.DOWN);
        if (!down.equals(new AABB(1, 0, 1, 16, 1, 16)))
            test.fail(String.format("Wall bound (%s) did not match for direction %s.", down, Direction.DOWN));
        AABB west = BlockSpaceUtil.getWallBounds(outerBounds, Direction.WEST);
        if (!west.equals(new AABB(0, 1, 1, 1, 16, 16)))
            test.fail(String.format("Wall bound (%s) did not match for direction %s.", west, Direction.WEST));
        AABB north = BlockSpaceUtil.getWallBounds(outerBounds, Direction.NORTH);
        if (!north.equals(new AABB(1, 1, 0, 16, 16, 1)))
            test.fail(String.format("Wall bound (%s) did not match for direction %s.", north, Direction.NORTH));
        AABB east = BlockSpaceUtil.getWallBounds(outerBounds, Direction.EAST);
        if (!east.equals(new AABB(15, 1, 1, 16, 16, 16)))
            test.fail(String.format("Wall bound (%s) did not match for direction %s.", east, Direction.EAST));
        AABB south = BlockSpaceUtil.getWallBounds(outerBounds, Direction.SOUTH);
        if (!south.equals(new AABB(0, 1, 15, 1, 16, 16)))
            test.fail(String.format("Wall bound (%s) did not match for direction %s.", south, Direction.SOUTH));*/
        BlockPos    upCenter = BlockSpaceUtil.centerWallBlockPos(outerBounds, Direction.UP);
        BlockPos  downCenter = BlockSpaceUtil.centerWallBlockPos(outerBounds, Direction.DOWN);
        BlockPos  westCenter = BlockSpaceUtil.centerWallBlockPos(outerBounds, Direction.WEST);
        BlockPos northCenter = BlockSpaceUtil.centerWallBlockPos(outerBounds, Direction.NORTH);
        BlockPos  eastCenter = BlockSpaceUtil.centerWallBlockPos(outerBounds, Direction.EAST);
        BlockPos southCenter = BlockSpaceUtil.centerWallBlockPos(outerBounds, Direction.SOUTH);
        test.assertTrue(   upCenter.equals(new BlockPos( 8, 14,  8)), String.format("%s center is not correct %s", Direction.UP, upCenter));
        test.assertTrue( downCenter.equals(new BlockPos( 8,  0,  8)), String.format("%s center is not correct %s", Direction.DOWN, downCenter));
        test.assertTrue( westCenter.equals(new BlockPos( 1,  7,  8)), String.format("%s center is not correct %s", Direction.WEST, westCenter));
        test.assertTrue(northCenter.equals(new BlockPos( 8,  7,  1)), String.format("%s center is not correct %s", Direction.NORTH, northCenter));
        test.assertTrue( eastCenter.equals(new BlockPos(15,  7,  8)), String.format("%s center is not correct %s", Direction.EAST, eastCenter));
        test.assertTrue(southCenter.equals(new BlockPos( 8,  7, 15)), String.format("%s center is not correct %s", Direction.SOUTH, southCenter));
        AABB up = BlockSpaceUtil.getWallBounds(outerBounds, Direction.UP);      // 8.5, 14.5,  8.5 ( 8, 14,  8)
        if (!up.equals(new AABB(1, 14, 1, 16, 15, 16)))
            test.fail(String.format("Wall bound (%s) did not match for direction %s.", up, Direction.UP));
        AABB down = BlockSpaceUtil.getWallBounds(outerBounds, Direction.DOWN);  // 8.5,  0.5,  8.5 ( 8,  0,  8)
        if (!down.equals(new AABB(1, 0, 1, 16, 1, 16)))
            test.fail(String.format("Wall bound (%s) did not match for direction %s.", down, Direction.DOWN));
        AABB west = BlockSpaceUtil.getWallBounds(outerBounds, Direction.WEST);  // 1.5,  7.5,  8.5 ( 1,  7,  8)
        if (!west.equals(new AABB(1, 0, 1, 2, 15, 16)))
            test.fail(String.format("Wall bound (%s) did not match for direction %s.", west, Direction.WEST));
        AABB north = BlockSpaceUtil.getWallBounds(outerBounds, Direction.NORTH);// 8.5,  7.5,  1.5 ( 8,  7,  1)
        if (!north.equals(new AABB(1, 0, 1, 16, 15, 2)))
            test.fail(String.format("Wall bound (%s) did not match for direction %s.", north, Direction.NORTH));

        AABB east = BlockSpaceUtil.getWallBounds(outerBounds, Direction.EAST);  //15.5,  7.5,  8.5 (15,  7,  8)
        if (!east.equals(new AABB(15, 0, 1, 16, 15, 16)))
            test.fail(String.format("Wall bound (%s) did not match for direction %s.", east, Direction.EAST));
        AABB south = BlockSpaceUtil.getWallBounds(outerBounds, Direction.SOUTH);// 8.5,  7.5, 15.5 ( 8,  7, 15)
        if (!south.equals(new AABB(1, 0, 15, 16, 15, 16)))
            test.fail(String.format("Wall bound (%s) did not match for direction %s.", south, Direction.SOUTH));
        test.succeed();
    }
}

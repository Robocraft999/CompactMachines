package dev.compactmods.machines.room.spawn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.room.spawn.IRoomSpawn;
import dev.compactmods.machines.api.codec.CodecExtensions;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public record RoomSpawn(Vec3 position, Vec2 rotation) implements IRoomSpawn {
    public static final Codec<RoomSpawn> CODEC = RecordCodecBuilder.create(i -> i.group(
            Vec3.CODEC.fieldOf("position").forGetter(RoomSpawn::position),
            CodecExtensions.VEC2.fieldOf("rotation").forGetter(RoomSpawn::rotation)
    ).apply(i, RoomSpawn::new));

}
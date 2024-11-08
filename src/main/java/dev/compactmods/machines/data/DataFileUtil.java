package dev.compactmods.machines.data;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.room.RoomRegistrar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataFileUtil {

    public static void ensureDirExists(Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static <T> T loadFileWithCodec(File file, Codec<T> codec) {
        try {
            //IOUtilities.cleanupTempFiles(Path.of(file.getParent()), file.getName());
            try (var is = new FileInputStream(file)) {
                final var tag = NbtIo.readCompressed(is);
                return codec.parse(NbtOps.INSTANCE, tag.contains("data") ? tag.getCompound("data") : new CompoundTag())
                        .getOrThrow(false, (e) -> {});
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

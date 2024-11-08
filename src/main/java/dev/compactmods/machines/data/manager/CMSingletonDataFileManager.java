package dev.compactmods.machines.data.manager;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.data.CMDataFile;
import dev.compactmods.machines.data.CodecHolder;
import dev.compactmods.machines.data.DataFileUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Function;

public class CMSingletonDataFileManager<T extends CMDataFile & CodecHolder<T>> implements IDataFileManager<T> {

    protected final MinecraftServer server;
    private final String dataKey;
    private @Nullable T instance;

    public CMSingletonDataFileManager(MinecraftServer server, String dataKey, T instance) {
        this.server = server;
        this.dataKey = dataKey;
        this.instance = instance;
    }

    public T data() {
        return this.instance;
    }

    private void ensureFileReady() {
        var dir = instance.getDataLocation(server);
        DataFileUtil.ensureDirExists(dir);
    }

    public void save() {
        if (instance != null) {
            ensureFileReady();

            var fullData = new CompoundTag();
            fullData.putString("version", instance.getDataVersion());

            var fileData = instance.codec()
                    .encodeStart(NbtOps.INSTANCE, instance)
                    .getOrThrow(false, (s) -> {});

            fullData.put("data", fileData);

            try {
                NbtIo.writeCompressed(fullData, instance.getDataLocation(server).resolve(dataKey + ".dat").toFile());
            } catch (IOException e) {
                LoggingUtil.modLog().error("Failed to write data: " + e.getMessage(), e);
            }
        }
    }
}

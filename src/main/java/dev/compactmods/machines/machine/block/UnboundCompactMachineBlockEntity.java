package dev.compactmods.machines.machine.block;

import dev.compactmods.machines.api.machine.block.IUnboundCompactMachineBlockEntity;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.capability.IMachineColorCapability;
import dev.compactmods.machines.machine.capability.MachineColorCapabilityImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class UnboundCompactMachineBlockEntity extends BlockEntity implements IUnboundCompactMachineBlockEntity {
    private @Nullable ResourceLocation templateId;
    private LazyOptional<IMachineColorCapability> capability;
    public UnboundCompactMachineBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(Machines.BlockEntities.UNBOUND_MACHINE.get(), pPos, pBlockState);
        this.templateId = null;
        capability = LazyOptional.of(MachineColorCapabilityImpl::getDefault);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);

        if (pTag.contains(NBT_TEMPLATE_ID))
            this.templateId = new ResourceLocation(pTag.getString(NBT_TEMPLATE_ID));
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);

        if (templateId != null)
            pTag.putString(NBT_TEMPLATE_ID, templateId.toString());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag data = super.getUpdateTag();
        saveAdditional(data);

        if (this.templateId != null)
            data.putString(NBT_TEMPLATE_ID, templateId.toString());
        return data;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        if (tag.contains(NBT_TEMPLATE_ID))
            templateId = new ResourceLocation(tag.getString(NBT_TEMPLATE_ID));
    }

    public void setTemplate(ResourceLocation template) {
        this.templateId = template;
        this.setChanged();
    }

    @Nullable
    public ResourceLocation templateId() {
        return templateId;
    }

    public Optional<RoomTemplate> template() {
        assert level != null;
        var f = this.templateId();
        System.out.println("TTT: '" + f + "'");

        //FIXME
        //var t = RoomTemplate.INVALID_TEMPLATE;//this.components().getOrDefault(Machines.DataComponents.ROOM_TEMPLATE.get(), RoomTemplate.INVALID_TEMPLATE);
        var t = new RoomTemplate(7, FastColor.ARGB32.color(255, 251, 242, 54));
        return Optional.ofNullable(t);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == Machines.Attachments.MACHINE_COLOR){
            return capability.cast();
        }
        return super.getCapability(cap);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        capability.invalidate();
    }
}

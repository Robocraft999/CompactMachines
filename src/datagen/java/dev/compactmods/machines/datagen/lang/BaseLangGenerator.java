package dev.compactmods.machines.datagen.lang;

import dev.compactmods.machines.api.core.Advancements;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.datagen.AdvancementLangBuilder;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.tunnel.Tunnels;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.function.Supplier;

import static org.apache.commons.lang3.StringUtils.capitalize;

public abstract class BaseLangGenerator extends LanguageProvider {

    private final String locale;

    public BaseLangGenerator(DataGenerator gen, String locale) {
        super(gen.getPackOutput(), Constants.MOD_ID, locale);
        this.locale = locale;
    }

    protected abstract String getSizeTranslation(RoomSize size);

    @SuppressWarnings("unused")
    protected String getDirectionTranslation(Direction dir) {
        return capitalize(dir.getSerializedName());
    }

    protected String getMachineTranslation() {
        return "Compact Machine";
    }

    @Override
    protected void addTranslations() {
        // Direction Names
        for (var dir : Direction.values()) {
            add(Constants.MOD_ID + ".direction." + dir.getSerializedName(), getDirectionTranslation(dir));
        }
    }

    protected void addTooltip(ResourceLocation id, String translation) {
        add(TranslationUtil.tooltipId(id), translation);
    }

    protected void addTunnel(Supplier<TunnelDefinition> tunnel, String name) {
        add(TranslationUtil.tunnelId(Tunnels.getRegistryId(tunnel.get())), name);
    }

    void addUpgradeItem(Supplier<RoomUpgrade> upgrade, String translation) {
        final var u = upgrade.get();
        if(u != null)
            add(u.getTranslationKey(), translation);
    }

    protected void addCreativeTab(ResourceLocation id, String translation) {
        add(Util.makeDescriptionId("itemGroup", id), translation);
    }

    protected AdvancementLangBuilder advancement(ResourceLocation advancement) {
        return new AdvancementLangBuilder(this, advancement);
    }

    protected void addCommand(ResourceLocation id, String translation) {
        this.add(TranslationUtil.commandId(id), translation);
    }

    protected void addMessage(ResourceLocation id, String translation) {
        this.add(TranslationUtil.messageId(id), translation);
    }
}

package dev.compactmods.machines.advancement;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.advancement.trigger.BasicPlayerAdvTrigger;
import dev.compactmods.machines.advancement.trigger.HowDidYouGetHereTrigger;
import dev.compactmods.machines.api.core.Advancements;
import dev.compactmods.machines.api.room.RoomSize;
import net.minecraft.advancements.CriteriaTriggers;

public class AdvancementTriggers {

    public static final BasicPlayerAdvTrigger RECURSIVE_ROOMS = CriteriaTriggers.register(new BasicPlayerAdvTrigger(Advancements.RECURSIVE_ROOMS));

    public static final HowDidYouGetHereTrigger HOW_DID_YOU_GET_HERE = CriteriaTriggers.register(new HowDidYouGetHereTrigger());

    public static void init() {
        CompactMachines.LOGGER.trace("Registering advancement triggers.");
    }
}

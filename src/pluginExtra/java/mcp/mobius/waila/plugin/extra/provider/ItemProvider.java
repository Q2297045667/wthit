package mcp.mobius.waila.plugin.extra.provider;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mcp.mobius.waila.api.IDataReader;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.component.ItemListComponent;
import mcp.mobius.waila.api.data.ItemData;
import mcp.mobius.waila.api.data.ProgressData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemProvider extends DataProvider<ItemData> {

    public static final ItemProvider INSTANCE = new ItemProvider();
    private static final CompoundTag EMPTY = new CompoundTag();

    protected ItemProvider() {
        super(ItemData.ID, ItemData.class, ItemData::new);
    }

    @Override
    protected void registerAdditions(IRegistrar registrar, int priority) {
        registrar.addSyncedConfig(ItemData.CONFIG_SYNC_NBT, false, false);
    }

    @Override
    protected void appendBody(ITooltip tooltip, IDataReader reader, IPluginConfig config, ResourceLocation objectId) {
        ProgressData progress = reader.get(ProgressData.class);
        if (progress == null || progress.ratio() == 0f) {
            super.appendBody(tooltip, reader, config, objectId);
        }
    }

    @Override
    protected void appendBody(ITooltip tooltip, ItemData data, IPluginConfig config, ResourceLocation objectId) {
        Map<Object, ItemStack> merged = new HashMap<>();
        Map<Item, Set<CompoundTag>> unique = new HashMap<>();

        for (ItemStack stack : data.items()) {
            Item item = stack.getItem();
            int count = stack.getCount();

            if (!data.syncNbt()) {
                if (unique.put(item, Set.of()) != null) {
                    merged.get(item).grow(count);
                } else {
                    merged.put(item, stack.copy());
                }
            } else {
                CompoundTag nbt = stack.getTag();
                if (nbt == null) nbt = EMPTY;

                if (unique.computeIfAbsent(item, i -> new HashSet<>()).add(nbt)) {
                    merged.put(new ItemWithNbt(item, nbt), stack.copy());
                } else {
                    merged.get(new ItemWithNbt(item, nbt)).grow(count);
                }
            }
        }

        if (merged.isEmpty()) return;

        tooltip.setLine(ItemData.ID, new ItemListComponent(merged.values().stream()
            .sorted(Comparator.comparingInt(ItemStack::getCount).reversed())
            .toList()));
    }

    private record ItemWithNbt(Item item, CompoundTag tag) {

    }

}

package dev.robottitan;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(RobotTitanMod.MODID)
public class RobotTitanMod {
    public static final String MODID = "robottitan";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<TitanEntity>> TITAN = ENTITIES.register("titan",
            () -> EntityType.Builder.of(TitanEntity::new, MobCategory.MISC)
                    .sized(2.8f, 6.4f)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .fireImmune()
                    .build("titan"));

    public static final RegistryObject<Item> CORE = ITEMS.register("titan_core",
            () -> new TitanCoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    public RobotTitanMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        ENTITIES.register(bus);
        bus.addListener(this::attributes);
        bus.addListener(this::addToTab);
    }

    private void attributes(EntityAttributeCreationEvent e) {
        e.put(TITAN.get(), TitanEntity.createAttributes().build());
    }

    private void addToTab(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.COMBAT) e.accept(CORE);
    }
}

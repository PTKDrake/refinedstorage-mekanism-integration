package com.refinedmods.refinedstorage.mekanism;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.storage.StorageContainerUpgradeRecipe;
import com.refinedmods.refinedstorage.common.storage.StorageContainerUpgradeRecipeSerializer;
import com.refinedmods.refinedstorage.common.support.SimpleItem;
import com.refinedmods.refinedstorage.mekanism.chemical.ChemicalGridExtractionStrategy;
import com.refinedmods.refinedstorage.mekanism.chemical.ChemicalGridInsertionStrategy;
import com.refinedmods.refinedstorage.mekanism.chemical.ChemicalResourceContainerInsertStrategy;
import com.refinedmods.refinedstorage.mekanism.chemical.ChemicalResourceFactory;
import com.refinedmods.refinedstorage.mekanism.chemical.ChemicalResourceType;
import com.refinedmods.refinedstorage.mekanism.chemical.ChemicalStorageDiskItem;
import com.refinedmods.refinedstorage.mekanism.chemical.ChemicalStorageMonitorInsertionStrategy;
import com.refinedmods.refinedstorage.mekanism.chemical.ChemicalStorageVariant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.refinedmods.refinedstorage.mekanism.MekanismIntegrationIdentifierUtil.createMekanismIntegrationIdentifier;

@Mod(MekanismIntegrationIdentifierUtil.MOD_ID)
public final class ModInitializer {
    public ModInitializer(final IEventBus eventBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            eventBus.addListener(ClientModInitializer::onClientSetup);
        }
        eventBus.addListener(this::setup);
        eventBus.addListener(this::registerCreativeModeTabListener);

        final DeferredRegister<Item> itemRegistry = DeferredRegister.create(
            BuiltInRegistries.ITEM,
            MekanismIntegrationIdentifierUtil.MOD_ID
        );
        for (final ChemicalStorageVariant variant : ChemicalStorageVariant.values()) {
            if (variant.getStoragePartId() != null) {
                Items.INSTANCE.setChemicalStoragePart(
                    variant,
                    itemRegistry.register(variant.getStoragePartId().getPath(), SimpleItem::new)
                );
            }
            Items.INSTANCE.setChemicalStorageDisk(
                variant,
                itemRegistry.register(variant.getStorageDiskId().getPath(), () -> new ChemicalStorageDiskItem(
                    RefinedStorageApi.INSTANCE.getStorageContainerItemHelper(),
                    variant
                ))
            );
        }
        itemRegistry.register(eventBus);

        final DeferredRegister<RecipeSerializer<?>> recipeSerializerRegistry = DeferredRegister.create(
            BuiltInRegistries.RECIPE_SERIALIZER,
            MekanismIntegrationIdentifierUtil.MOD_ID
        );
        recipeSerializerRegistry.register(
            "chemical_storage_disk_upgrade",
            () -> new StorageContainerUpgradeRecipeSerializer<>(
                ChemicalStorageVariant.values(),
                to -> new StorageContainerUpgradeRecipe<>(
                    ChemicalStorageVariant.values(), to, Items.INSTANCE::getChemicalStorageDisk
                )
            )
        );
        recipeSerializerRegistry.register(eventBus);
    }

    private void setup(final FMLCommonSetupEvent e) {
        RefinedStorageApi.INSTANCE.getResourceTypeRegistry().register(
            createMekanismIntegrationIdentifier("chemical"),
            ChemicalResourceType.INSTANCE
        );
        RefinedStorageApi.INSTANCE.getAlternativeResourceFactories().add(new ChemicalResourceFactory());
        RefinedStorageApi.INSTANCE.getStorageTypeRegistry().register(
            createMekanismIntegrationIdentifier("chemical"),
            ChemicalResourceType.STORAGE_TYPE
        );
        RefinedStorageApi.INSTANCE.addGridInsertionStrategyFactory(ChemicalGridInsertionStrategy::new);
        RefinedStorageApi.INSTANCE.addGridExtractionStrategyFactory(ChemicalGridExtractionStrategy::new);
        RefinedStorageApi.INSTANCE.addStorageMonitorInsertionStrategy(new ChemicalStorageMonitorInsertionStrategy());
        RefinedStorageApi.INSTANCE.addResourceContainerInsertStrategy(new ChemicalResourceContainerInsertStrategy());
    }

    private void registerCreativeModeTabListener(final BuildCreativeModeTabContentsEvent e) {
        final ResourceKey<CreativeModeTab> creativeModeTab = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            RefinedStorageApi.INSTANCE.getCreativeModeTabId()
        );
        if (!e.getTabKey().equals(creativeModeTab)) {
            return;
        }
        for (final ChemicalStorageVariant variant : ChemicalStorageVariant.values()) {
            if (variant.getStoragePartId() != null) {
                e.accept(Items.INSTANCE.getChemicalStoragePart(variant));
            }
        }
        for (final ChemicalStorageVariant variant : ChemicalStorageVariant.values()) {
            e.accept(Items.INSTANCE.getChemicalStorageDisk(variant));
        }
    }
}

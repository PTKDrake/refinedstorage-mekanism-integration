package com.refinedmods.refinedstorage.mekanism;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.mekanism.grid.ChemicalGridInsertionHint;
import com.refinedmods.refinedstorage.mekanism.storage.ChemicalStorageVariant;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import static com.refinedmods.refinedstorage.mekanism.MekanismIntegrationIdentifierUtil.createMekanismIntegrationIdentifier;

public final class ClientModInitializer {
    private ClientModInitializer() {
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent e) {
        RefinedStorageApi.INSTANCE.registerResourceRendering(
            ChemicalResource.class,
            new ChemicalResourceRendering()
        );
        RefinedStorageApi.INSTANCE.addAlternativeGridInsertionHint(new ChemicalGridInsertionHint());
        final ResourceLocation diskModel = createMekanismIntegrationIdentifier("block/disk/chemical_disk");
        for (final ChemicalStorageVariant variant : ChemicalStorageVariant.values()) {
            RefinedStorageApi.INSTANCE.getStorageContainerItemHelper().registerDiskModel(
                Items.getChemicalStorageDisk(variant),
                diskModel
            );
        }
    }
}

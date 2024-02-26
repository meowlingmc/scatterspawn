package de.saschat.scatterspawn.api;

import de.saschat.scatterspawn.logic.Scatterer;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.function.Supplier;

public interface ScattererProviderEntrypoint {
    void register(Map<ResourceLocation, Supplier<Scatterer>> scattererRegistry);
}

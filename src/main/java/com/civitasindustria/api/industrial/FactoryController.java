package com.civitasindustria.api.industrial;
import com.civitasindustria.domain.Commissioning;
/** Consolidated-production diagnostics, independent of Minecraft implementation classes. */
public interface FactoryController {
    Commissioning.Stage commissioningStage();
    int parallelBatchSize();
    int operationProgress();
}

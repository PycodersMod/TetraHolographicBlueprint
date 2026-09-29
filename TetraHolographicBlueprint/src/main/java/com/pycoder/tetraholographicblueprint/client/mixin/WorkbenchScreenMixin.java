package com.pycoder.tetraholographicblueprint.client.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import com.pycoder.tetraholographicblueprint.client.blueprint.BlueprintSession;
import com.pycoder.tetraholographicblueprint.client.blueprint.BlueprintSourceMode;
import com.pycoder.tetraholographicblueprint.client.blueprint.WorkbenchBlueprintInteractionState;
import com.pycoder.tetraholographicblueprint.client.blueprint.WorkbenchBlueprintPanelState;
import com.pycoder.tetraholographicblueprint.client.blueprint.WorkbenchBlueprintUiState;
import com.pycoder.tetraholographicblueprint.client.blueprint.MaterialManifest;
import com.pycoder.tetraholographicblueprint.client.blueprint.ProcessingCondition;
import com.pycoder.tetraholographicblueprint.client.blueprint.ProcessingConditionSummary;
import com.pycoder.tetraholographicblueprint.client.blueprint.TetraProcessingConditionReader;
import com.pycoder.tetraholographicblueprint.client.blueprint.TetraMaterialManifestReader;
import com.pycoder.tetraholographicblueprint.client.blueprint.TetraSchematicCatalog;
import com.pycoder.tetraholographicblueprint.client.blueprint.TetraSchematicPreviewEntry;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import se.mickelus.mutil.gui.GuiElement;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchScreen;
import se.mickelus.tetra.module.schematic.CraftingContext;

@Mixin(WorkbenchScreen.class)
public abstract class WorkbenchScreenMixin {
    @Shadow(remap = false) @Final private WorkbenchTile tileEntity;
    @Shadow(remap = false) @Final private GuiElement defaultGui;
    @Shadow(remap = false) @Final private GuiElement moduleList;
    @Shadow(remap = false) @Final private GuiElement actionList;
    @Shadow(remap = false) @Final private GuiElement slotDetail;

    @Unique
    private BlueprintSession tetraHolographicBlueprint$session;
    @Unique
    private boolean tetraHolographicBlueprint$modeActive;

    @Unique
    private static final int MODE_BUTTON_WIDTH = 96;
    @Unique
    private static final int MODE_BUTTON_HEIGHT = 18;
    @Unique
    private static final int PAGE_BUTTON_WIDTH = 50;
    @Unique
    private static final int PAGE_BUTTON_HEIGHT = 18;
    @Unique
    private static final int PANEL_WIDTH = 286;
    @Unique
    private static final int PANEL_HEIGHT = 128;
    @Unique
    private static final int PAGE_GRID_COLUMNS = 2;
    @Unique
    private static final int PAGE_GRID_ROWS = 5;
    @Unique
    private static final int PAGE_GRID_CELL_WIDTH = 136;
    @Unique
    private static final int PAGE_GRID_CELL_HEIGHT = 16;
    @Unique
    private static final int VANILLA_GUI_WIDTH = 320;
    @Unique
    private static final int VANILLA_GUI_HEIGHT = 240;

    @Inject(method = "init", at = @At("TAIL"))
    private void tetraHolographicBlueprint$createSession(CallbackInfo callbackInfo) {
        int guiWidth = tetraHolographicBlueprint$windowWidth();
        int guiHeight = tetraHolographicBlueprint$windowHeight();
        tetraHolographicBlueprint$setContainerLayout(
                6,
                Math.max(6, guiHeight - VANILLA_GUI_HEIGHT - 6),
                VANILLA_GUI_WIDTH,
                VANILLA_GUI_HEIGHT);
        tetraHolographicBlueprint$syncWorkbenchVisibility();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void tetraHolographicBlueprint$renderOverlay(
            GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo callbackInfo) {
        WorkbenchBlueprintUiState uiState = tetraHolographicBlueprint$uiState();
        if (uiState == null) {
            return;
        }

        tetraHolographicBlueprint$syncWorkbenchVisibility();

        int buttonX = tetraHolographicBlueprint$modeButtonX();
        int buttonY = tetraHolographicBlueprint$modeButtonY();
        tetraHolographicBlueprint$drawButton(
                guiGraphics,
                buttonX,
                buttonY,
                MODE_BUTTON_WIDTH,
                MODE_BUTTON_HEIGHT,
                tetraHolographicBlueprint$modeActive ? 0xFF244824 : 0xFF1A1A1A,
                uiState.modeButtonLabel());

        if (uiState.showPageNavigation()) {
            int prevX = buttonX;
            int prevY = buttonY + MODE_BUTTON_HEIGHT + 4;
            int nextX = buttonX + PAGE_BUTTON_WIDTH + 4;
            int nextY = prevY;
            tetraHolographicBlueprint$drawButton(
                    guiGraphics,
                    prevX,
                    prevY,
                    PAGE_BUTTON_WIDTH,
                    PAGE_BUTTON_HEIGHT,
                    uiState.canSelectPreviousPage() ? 0xFF2A2A2A : 0xFF141414,
                    "上一页");
            tetraHolographicBlueprint$drawButton(
                    guiGraphics,
                    nextX,
                    nextY,
                    PAGE_BUTTON_WIDTH,
                    PAGE_BUTTON_HEIGHT,
                    uiState.canSelectNextPage() ? 0xFF2A2A2A : 0xFF141414,
                    "下一页");
        }

        if (!tetraHolographicBlueprint$modeActive || tetraHolographicBlueprint$session == null) {
            return;
        }

        WorkbenchBlueprintPanelState panelState = tetraHolographicBlueprint$panelState();
        int infoX = tetraHolographicBlueprint$windowWidth() / 2 - 146;
        int infoY = 8;
        tetraHolographicBlueprint$drawPanelFrame(guiGraphics, infoX, infoY, 292, 40, "蓝图信息");
        int infoTextY = infoY + 12;
        if (panelState.statusLine().isPresent()) {
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(panelState.statusLine().orElseThrow()),
                    infoX + 8,
                    infoTextY,
                    0xFFE8E8E8,
                    false);
            infoTextY += 10;
        }
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(panelState.previewHeader()),
                infoX + 8,
                infoTextY,
                0xFFE8E8E8,
                false);
        infoTextY += 10;
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(panelState.pageLabel()),
                infoX + 196,
                infoY + 12,
                0xFFE8E8E8,
                false);
        if (panelState.selectedPreviewLine().isPresent()) {
            int selectedLineY = infoTextY;
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(panelState.selectedPreviewLine().orElseThrow()),
                    infoX + 8,
                    selectedLineY,
                    0xFFCFCFCF,
                    false);
        }

        int schematicPanelX = Math.max(tetraHolographicBlueprint$guiLeft() + tetraHolographicBlueprint$guiWidth() + 12, tetraHolographicBlueprint$windowWidth() - PANEL_WIDTH - 6);
        int schematicPanelY = Math.max(68, tetraHolographicBlueprint$guiTop() + 18);
        tetraHolographicBlueprint$drawPanelFrame(guiGraphics, schematicPanelX, schematicPanelY, PANEL_WIDTH, PANEL_HEIGHT, "原理图区域");
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(panelState.pageLabel()),
                schematicPanelX + PANEL_WIDTH - 56,
                schematicPanelY + 6,
                0xFFBFBFBF,
                false);

        int gridX = schematicPanelX + 8;
        int gridY = schematicPanelY + 20;
        List<WorkbenchBlueprintPanelState.PreviewEntryView> entries = panelState.pageEntries();
        for (int index = 0; index < entries.size(); index++) {
            WorkbenchBlueprintPanelState.PreviewEntryView entry = entries.get(index);
            int column = index % PAGE_GRID_COLUMNS;
            int row = index / PAGE_GRID_COLUMNS;
            int cellX = gridX + column * PAGE_GRID_CELL_WIDTH;
            int cellY = gridY + row * PAGE_GRID_CELL_HEIGHT;
            tetraHolographicBlueprint$drawEntryCell(guiGraphics, cellX, cellY, PAGE_GRID_CELL_WIDTH - 4, PAGE_GRID_CELL_HEIGHT - 2, entry.selected());
            String label = Minecraft.getInstance().font.substrByWidth(Component.literal(entry.label()), PAGE_GRID_CELL_WIDTH - 10).getString();
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(label),
                    cellX + 4,
                    cellY + 5,
                    entry.selected() ? 0xFFFFFFFF : 0xFFD6D6D6,
                    false);
        }

        int materialsPanelY = schematicPanelY + PANEL_HEIGHT + 10;
        int materialsPanelHeight = Math.max(70, tetraHolographicBlueprint$windowHeight() - materialsPanelY - 12);
        tetraHolographicBlueprint$drawPanelFrame(guiGraphics, schematicPanelX, materialsPanelY, PANEL_WIDTH, materialsPanelHeight, "对应材料");
        int materialsY = materialsPanelY + 16;
        for (String materialLine : panelState.materialLines()) {
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(materialLine),
                    schematicPanelX + 8,
                    materialsY,
                    0xFFE8E8E8,
                    false);
            materialsY += 10;
        }
        if (!panelState.conditionLines().isEmpty()) {
            materialsY += 4;
            for (String conditionLine : panelState.conditionLines()) {
                guiGraphics.drawString(
                        Minecraft.getInstance().font,
                        Component.literal(conditionLine),
                        schematicPanelX + 8,
                        materialsY,
                        0xFFCFCFCF,
                        false);
                materialsY += 10;
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void tetraHolographicBlueprint$handleModeButton(
            double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (button != 0) {
            return;
        }

        if (tetraHolographicBlueprint$interactionState().isModeButtonClick(
                mouseX,
                mouseY,
                tetraHolographicBlueprint$modeButtonX(),
                tetraHolographicBlueprint$modeButtonY(),
                tetraHolographicBlueprint$modeButtonX() + MODE_BUTTON_WIDTH,
                tetraHolographicBlueprint$modeButtonY() + MODE_BUTTON_HEIGHT)) {
            tetraHolographicBlueprint$toggleMode();
            callbackInfo.setReturnValue(true);
            return;
        }

        if (!tetraHolographicBlueprint$modeActive || tetraHolographicBlueprint$session == null) {
            return;
        }

        WorkbenchBlueprintUiState uiState = tetraHolographicBlueprint$uiState();
        int prevX = tetraHolographicBlueprint$pagePreviousButtonX();
        int prevY = tetraHolographicBlueprint$pageButtonsY();
        if (uiState.showPageNavigation() && tetraHolographicBlueprint$interactionState().isPreviousPageClick(
                mouseX,
                mouseY,
                prevX,
                prevY,
                prevX + PAGE_BUTTON_WIDTH,
                prevY + PAGE_BUTTON_HEIGHT)) {
            tetraHolographicBlueprint$session.selectPreviousSchematicPage();
            tetraHolographicBlueprint$applySelectedPreview();
            callbackInfo.setReturnValue(true);
            return;
        }

        int nextX = tetraHolographicBlueprint$pageNextButtonX();
        int nextY = tetraHolographicBlueprint$pageButtonsY();
        if (uiState.showPageNavigation() && tetraHolographicBlueprint$interactionState().isNextPageClick(
                mouseX,
                mouseY,
                nextX,
                nextY,
                nextX + PAGE_BUTTON_WIDTH,
                nextY + PAGE_BUTTON_HEIGHT)) {
            tetraHolographicBlueprint$session.selectNextSchematicPage();
            tetraHolographicBlueprint$applySelectedPreview();
            callbackInfo.setReturnValue(true);
            return;
        }

        WorkbenchBlueprintPanelState panelState = tetraHolographicBlueprint$panelState();
        int pageEntryIndex = tetraHolographicBlueprint$pageEntryIndexAt(mouseX, mouseY, panelState);
        if (pageEntryIndex >= 0) {
            tetraHolographicBlueprint$session.selectSchematicIndex(panelState.pageEntries().get(pageEntryIndex).globalIndex());
            tetraHolographicBlueprint$applySelectedPreview();
            callbackInfo.setReturnValue(true);
            return;
        }
    }

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void tetraHolographicBlueprint$refreshSession(CallbackInfo callbackInfo) {
        if (tetraHolographicBlueprint$modeActive) {
            tetraHolographicBlueprint$syncSessionData();
        }
    }

    @Unique
    private void tetraHolographicBlueprint$syncSessionData() {
        if (!tetraHolographicBlueprint$modeActive || tetraHolographicBlueprint$session == null) {
            return;
        }

        ProcessingConditionSummary sessionConditions =
                tetraHolographicBlueprint$session.processingConditions();
        sessionConditions.clear();
        tetraHolographicBlueprint$session.fullMaterialManifest().clear();
        ItemStack target = tileEntity.getTargetItemStack();
        String slot = tileEntity.getCurrentSlot();
        if (target.isEmpty() || slot == null || slot.isBlank() || tileEntity.getLevel() == null
                || Minecraft.getInstance().player == null) {
            tetraHolographicBlueprint$session.setSchematicEntries(java.util.List.of());
            return;
        }

        CraftingContext context = new CraftingContext(
                tileEntity.getLevel(),
                tileEntity.getBlockPos(),
                tileEntity.getBlockState(),
                Minecraft.getInstance().player,
                target,
                slot,
                tileEntity.getUnlockedSchematics());
        tetraHolographicBlueprint$session.setSchematicEntries(
                TetraSchematicCatalog.evaluateForContext(context, false));

        if (tetraHolographicBlueprint$applySelectedPreview()) {
            return;
        }

        var schematic = tileEntity.getCurrentSchematic();
        if (schematic == null) {
            return;
        }

        MaterialManifest fullMaterials = TetraMaterialManifestReader.read(
                schematic,
                target,
                tileEntity.getMaterials());
        for (MaterialManifest.MaterialEntry entry : fullMaterials.entries()) {
            tetraHolographicBlueprint$session.fullMaterialManifest().add(entry.stack(), entry.quantity());
        }

        ProcessingConditionSummary current = TetraProcessingConditionReader.read(
                schematic,
                target,
                tileEntity.getMaterials());
        for (var entry : current.minimumToolLevels().entrySet()) {
            sessionConditions.addToolRequirement(entry.getKey(), entry.getValue());
        }
        for (ProcessingCondition condition : current.conditions()) {
            sessionConditions.addCondition(condition.kind(), condition.identity(), condition.displayText());
        }
    }

    @Unique
    private boolean tetraHolographicBlueprint$applySelectedPreview() {
        if (tetraHolographicBlueprint$session == null) {
            return false;
        }
        var selected = tetraHolographicBlueprint$session.selectedSchematicEntry();
        if (selected.isEmpty()) {
            return false;
        }

        tetraHolographicBlueprint$session.fullMaterialManifest().clear();
        for (MaterialManifest.MaterialEntry entry : selected.get().fullMaterialManifest().entries()) {
            tetraHolographicBlueprint$session.fullMaterialManifest().add(entry.stack(), entry.quantity());
        }

        ProcessingConditionSummary destination = tetraHolographicBlueprint$session.processingConditions();
        destination.clear();
        ProcessingConditionSummary source = selected.get().processingConditions();
        for (var entry : source.minimumToolLevels().entrySet()) {
            destination.addToolRequirement(entry.getKey(), entry.getValue());
        }
        for (ProcessingCondition condition : source.conditions()) {
            destination.addCondition(condition.kind(), condition.identity(), condition.displayText());
        }
        return true;
    }

    @Unique
    private void tetraHolographicBlueprint$toggleMode() {
        tetraHolographicBlueprint$modeActive = !tetraHolographicBlueprint$modeActive;
        tetraHolographicBlueprint$syncWorkbenchVisibility();
        if (tetraHolographicBlueprint$modeActive) {
            tetraHolographicBlueprint$session = BlueprintSession.empty(BlueprintSourceMode.WORKBENCH_MODIFICATION);
            tetraHolographicBlueprint$syncSessionData();
        } else {
            if (tetraHolographicBlueprint$session != null) {
                tetraHolographicBlueprint$session.close();
            }
            tetraHolographicBlueprint$session = null;
        }
    }

    @Unique
    private WorkbenchBlueprintUiState tetraHolographicBlueprint$uiState() {
        if (tetraHolographicBlueprint$session == null) {
            return WorkbenchBlueprintUiState.empty(tetraHolographicBlueprint$modeActive);
        }
        return WorkbenchBlueprintUiState.from(tetraHolographicBlueprint$session, tetraHolographicBlueprint$modeActive);
    }

    @Unique
    private WorkbenchBlueprintPanelState tetraHolographicBlueprint$panelState() {
        if (tetraHolographicBlueprint$session == null) {
            return WorkbenchBlueprintPanelState.hidden();
        }
        return WorkbenchBlueprintPanelState.from(tetraHolographicBlueprint$session, tetraHolographicBlueprint$modeActive);
    }

    @Unique
    private WorkbenchBlueprintInteractionState tetraHolographicBlueprint$interactionState() {
        if (tetraHolographicBlueprint$session == null) {
            return WorkbenchBlueprintInteractionState.of(
                    tetraHolographicBlueprint$modeActive,
                    false,
                    false);
        }
        boolean showPagingControls = tetraHolographicBlueprint$session.schematicEntries().size() > 1;
        boolean hasSelectedSchematic = tetraHolographicBlueprint$session.selectedSchematicEntry().isPresent();
        return WorkbenchBlueprintInteractionState.of(
                tetraHolographicBlueprint$modeActive,
                showPagingControls,
                hasSelectedSchematic);
    }

    @Unique
    private int tetraHolographicBlueprint$modeButtonX() {
        return Math.max(6, tetraHolographicBlueprint$windowWidth() - MODE_BUTTON_WIDTH - 6);
    }

    @Unique
    private int tetraHolographicBlueprint$modeButtonY() {
        return 6;
    }

    @Unique
    private int tetraHolographicBlueprint$pageButtonsY() {
        return tetraHolographicBlueprint$modeButtonY() + MODE_BUTTON_HEIGHT + 4;
    }

    @Unique
    private int tetraHolographicBlueprint$pagePreviousButtonX() {
        return tetraHolographicBlueprint$modeButtonX();
    }

    @Unique
    private int tetraHolographicBlueprint$pageNextButtonX() {
        return tetraHolographicBlueprint$modeButtonX() + PAGE_BUTTON_WIDTH + 4;
    }

    @Unique
    private void tetraHolographicBlueprint$drawButton(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int buttonWidth,
            int buttonHeight,
            int fillColor,
            String label) {
        guiGraphics.fill(x, y, x + buttonWidth, y + buttonHeight, 0xFF090909);
        guiGraphics.fill(x + 1, y + 1, x + buttonWidth - 1, y + buttonHeight - 1, fillColor);
        guiGraphics.fill(x + 1, y + 1, x + buttonWidth - 1, y + 2, 0xFF5A5A5A);
        guiGraphics.fill(x + 1, y + buttonHeight - 2, x + buttonWidth - 1, y + buttonHeight - 1, 0xFF111111);
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(label),
                x + 6,
                y + 5,
                0xFFF0F0F0,
                false);
    }

    @Unique
    private void tetraHolographicBlueprint$drawPanelFrame(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int panelWidth,
            int panelHeight,
            String title) {
        guiGraphics.fill(x, y, x + panelWidth, y + panelHeight, 0xC0101010);
        guiGraphics.fill(x, y, x + panelWidth, y + 1, 0xFF6A6A6A);
        guiGraphics.fill(x, y + panelHeight - 1, x + panelWidth, y + panelHeight, 0xFF080808);
        guiGraphics.fill(x, y, x + 1, y + panelHeight, 0xFF5A5A5A);
        guiGraphics.fill(x + panelWidth - 1, y, x + panelWidth, y + panelHeight, 0xFF080808);
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(title),
                x + 6,
                y + 5,
                0xFFF2F2F2,
                false);
    }

    @Unique
    private void tetraHolographicBlueprint$drawEntryCell(
            GuiGraphics guiGraphics,
            int x,
            int y,
            int cellWidth,
            int cellHeight,
            boolean selected) {
        int innerColor = selected ? 0xFF283828 : 0xFF191919;
        int outerColor = selected ? 0xFF7FAF7F : 0xFF565656;
        guiGraphics.fill(x, y, x + cellWidth, y + cellHeight, 0xFF0B0B0B);
        guiGraphics.fill(x + 1, y + 1, x + cellWidth - 1, y + cellHeight - 1, innerColor);
        guiGraphics.fill(x + 1, y + 1, x + cellWidth - 1, y + 2, outerColor);
        guiGraphics.fill(x + 1, y + cellHeight - 2, x + cellWidth - 1, y + cellHeight - 1, 0xFF111111);
    }

    @Unique
    private int tetraHolographicBlueprint$pageEntryIndexAt(double mouseX, double mouseY, WorkbenchBlueprintPanelState panelState) {
        if (panelState == null || panelState.pageEntries().isEmpty()) {
            return -1;
        }
        int panelX = Math.max(tetraHolographicBlueprint$guiLeft() + tetraHolographicBlueprint$guiWidth() + 16, tetraHolographicBlueprint$windowWidth() - PANEL_WIDTH - 6);
        int panelY = Math.max(68, tetraHolographicBlueprint$guiTop() + 18);
        int gridX = panelX + 8;
        int gridY = panelY + 20;
        int relativeX = (int) mouseX - gridX;
        int relativeY = (int) mouseY - gridY;
        if (relativeX < 0 || relativeY < 0) {
            return -1;
        }
        int column = relativeX / PAGE_GRID_CELL_WIDTH;
        int row = relativeY / PAGE_GRID_CELL_HEIGHT;
        if (column < 0 || column >= PAGE_GRID_COLUMNS || row < 0 || row >= PAGE_GRID_ROWS) {
            return -1;
        }
        int index = row * PAGE_GRID_COLUMNS + column;
        return index < panelState.pageEntries().size() ? index : -1;
    }

    @Unique
    private int tetraHolographicBlueprint$windowWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    @Unique
    private int tetraHolographicBlueprint$windowHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    @Unique
    private void tetraHolographicBlueprint$syncWorkbenchVisibility() {
        boolean showBlueprint = tetraHolographicBlueprint$modeActive;
        actionList.setVisible(!showBlueprint);
        slotDetail.setVisible(!showBlueprint);
        moduleList.setVisible(true);
        defaultGui.setVisible(true);
    }

    @Unique
    private int tetraHolographicBlueprint$guiLeft() {
        return ((WorkbenchScreen) (Object) this).getGuiLeft();
    }

    @Unique
    private int tetraHolographicBlueprint$guiTop() {
        return ((WorkbenchScreen) (Object) this).getGuiTop();
    }

    @Unique
    private int tetraHolographicBlueprint$guiWidth() {
        return ((WorkbenchScreen) (Object) this).getXSize();
    }

    @Unique
    private int tetraHolographicBlueprint$guiHeight() {
        return ((WorkbenchScreen) (Object) this).getYSize();
    }

    @Unique
    private void tetraHolographicBlueprint$setContainerLayout(int left, int top, int imageWidth, int imageHeight) {
        tetraHolographicBlueprint$setAbstractContainerScreenField("leftPos", left);
        tetraHolographicBlueprint$setAbstractContainerScreenField("topPos", top);
        tetraHolographicBlueprint$setAbstractContainerScreenField("imageWidth", imageWidth);
        tetraHolographicBlueprint$setAbstractContainerScreenField("imageHeight", imageHeight);
    }

    @Unique
    private void tetraHolographicBlueprint$setAbstractContainerScreenField(String fieldName, int value) {
        try {
            var field = AbstractContainerScreen.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setInt(this, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to update container layout field: " + fieldName, exception);
        }
    }
}

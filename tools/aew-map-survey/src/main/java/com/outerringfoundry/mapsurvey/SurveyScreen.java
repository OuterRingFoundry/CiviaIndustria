package com.outerringfoundry.mapsurvey;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SurveyScreen extends Screen {
    private final Screen parent;
    private int radius = 16, seconds = 30;
    private Button start, stop;
    public SurveyScreen(Screen parent) { super(Component.literal("AEW Map Survey")); this.parent = parent; }
    @Override protected void init() {
        int x = width / 2 - 145, y = height / 2 - 68;
        addRenderableWidget(new NumberSlider(x, y, 290, radius, 2, 32, "Radius (chunks)", v -> radius = v));
        addRenderableWidget(new NumberSlider(x, y + 25, 290, seconds, 5, 120, "Duration (seconds)", v -> seconds = v));
        start = addRenderableWidget(Button.builder(Component.literal("Start survey"), b -> MapSurvey.start(radius, seconds))
                .bounds(x, y + 54, 142, 20).build());
        stop = addRenderableWidget(Button.builder(Component.literal("Stop and restore"), b -> MapSurvey.stop("Stopped"))
                .bounds(x + 148, y + 54, 142, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(x, Math.min(height / 2 + 105, height - 24), 290, 20).build());
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        start.active = !MapSurvey.active() && minecraft.level != null;
        stop.active = MapSurvey.active();
        super.render(g, mouseX, mouseY, partialTick);
        int x = width / 2, top = height / 2 - 100;
        g.drawCenteredString(font, title, x, top, 0xFFFFFF);
        int effective = Math.min(radius, Math.min(MapSurvey.serverLimit(), MapSurvey.clientMaximum()));
        g.drawCenteredString(font, "Server limit: " + MapSurvey.serverLimit() + " chunks | Selected radius: ~" + Math.max(0, effective) * 16 + " blocks", x, top + 16, 0xB8DCED);
        g.drawCenteredString(font, MapSurvey.status(), x, height / 2 + 18, 0xFFFFFF);
        String[] lines = {
            "The area follows you. Chunks arrive at the server's pace.",
            "Live changes arrive while chunks are in your viewing range.",
            "Cached map areas can become stale after the timer ends.",
            "This temporarily increases rendering, memory and network use.",
            MapSurvey.hasMap() ? "Xaero records terrain when map writing is enabled." : "Xaero World Map is absent: terrain will not be saved by this tool."
        };
        for (int i = 0; i < lines.length; i++) g.drawCenteredString(font, lines[i], x, height / 2 + 35 + i * 12, 0xC0C0C0);
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { minecraft.setScreen(parent); }
    private static final class NumberSlider extends AbstractSliderButton {
        private final int min, max;
        private final String label;
        private final java.util.function.IntConsumer changed;
        NumberSlider(int x, int y, int width, int initial, int min, int max, String label, java.util.function.IntConsumer changed) {
            super(x, y, width, 20, Component.empty(), (double)(initial - min) / (max - min));
            this.min = min; this.max = max; this.label = label; this.changed = changed; updateMessage();
        }
        private int selected() { return min + (int)Math.round(value * (max - min)); }
        @Override protected void updateMessage() { setMessage(Component.literal(label + ": " + selected())); }
        @Override protected void applyValue() { changed.accept(selected()); }
    }
}

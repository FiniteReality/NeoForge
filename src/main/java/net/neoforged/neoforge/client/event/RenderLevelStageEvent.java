/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.Pair;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * Fires at various times during LevelRenderer.renderLevel.
 * Check {@link #getStage} to render during the appropriate time for your use case.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}. </p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}. </p>
 */
public class RenderLevelStageEvent extends Event {
    private final Stage stage;
    private final LevelRenderer levelRenderer;
    private final PoseStack poseStack;
    private final Matrix4f modelViewMatrix;
    private final Matrix4f projectionMatrix;
    private final int renderTick;
    private final DeltaTracker partialTick;
    private final Camera camera;
    private final Frustum frustum;

    public RenderLevelStageEvent(Stage stage, LevelRenderer levelRenderer, @Nullable PoseStack poseStack, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, int renderTick, DeltaTracker partialTick, Camera camera, Frustum frustum) {
        this.stage = stage;
        this.levelRenderer = levelRenderer;
        this.poseStack = poseStack != null ? poseStack : new PoseStack();
        this.modelViewMatrix = modelViewMatrix;
        this.projectionMatrix = projectionMatrix;
        this.renderTick = renderTick;
        this.partialTick = partialTick;
        this.camera = camera;
        this.frustum = frustum;
    }

    /**
     * {@return the current {@linkplain Stage stage} that is being rendered. Check this before doing rendering to ensure
     * that rendering happens at the appropriate time.}
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * {@return the level renderer}
     */
    public LevelRenderer getLevelRenderer() {
        return levelRenderer;
    }

    /**
     * {@return the pose stack used for rendering}
     */
    public PoseStack getPoseStack() {
        return poseStack;
    }

    /**
     * {@return the model view matrix used for rendering}
     */
    public Matrix4f getModelViewMatrix() {
        return modelViewMatrix;
    }

    /**
     * {@return the projection matrix}
     */
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    /**
     * {@return the current "ticks" value in the {@linkplain LevelRenderer level renderer}}
     */
    public int getRenderTick() {
        return renderTick;
    }

    /**
     * {@return the current partialTick value used for rendering}
     */
    public DeltaTracker getPartialTick() {
        return partialTick;
    }

    /**
     * {@return the camera}
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * {@return the frustum}
     */
    public Frustum getFrustum() {
        return frustum;
    }

    /**
     * Use to create a custom {@linkplain RenderLevelStageEvent.Stage stages}.
     * Fired after the LevelRenderer has been created.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}. </p>
     *
     * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}. </p>
     */
    public static class RegisterStageEvent extends Event implements IModBusEvent {
        /**
         * @param name       The name of your Stage.
         * @param renderType
         *                   If not null, called automatically by LevelRenderer.renderChunkLayer if the RenderType passed into it matches this one.
         *                   If null, needs to be called manually by whoever implements it.
         *
         * @throws IllegalArgumentException if the RenderType passed is already mapped to a Stage.
         */
        public Stage register(ResourceLocation name, @Nullable RenderType renderType) throws IllegalArgumentException {
            return new Stage(name.toString(), renderType); //Stage(name, renderType);
        }
    }

    /**
     * A time during level rendering for you to render custom things into the world.
     * 
     * @see RegisterStageEvent
     */
    public static class Stage {
        /**
         * Use this to render custom objects into the skybox before it is rendered.
         * Called regardless of if they sky actually renders or not.
         */
        public static final Stage BEFORE_SKY = new Stage("before_sky", null);
        /**
         * Use this to render custom objects into the skybox after it is rendered.
         * Called regardless of if they sky actually renders or not.
         */
        public static final Stage AFTER_SKY = new Stage("after_sky", null);

        /**
         * Use this to render custom block-like geometry into the world, before vanilla renders any.
         */
        public static final Stage BEFORE_SOLID_BLOCKS = new Stage("before_solid_blocks", RenderType.solid());
        /**
         * Use this to render custom block-like geometry into the world, after vanilla renders any.
         */
        public static final Stage AFTER_SOLID_BLOCKS = new Stage("after_solid_blocks", RenderType.solid());

        /**
         * Use this to render custom block-like geometry into the world, before vanilla renders any.
         */
        public static final Stage BEFORE_CUTOUT_MIPPED_BLOCKS_BLOCKS = new Stage("before_cutout_mipped_blocks", RenderType.cutoutMipped());
        /**
         * Use this to render custom block-like geometry into the world, after vanilla renders any.
         */
        public static final Stage AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS = new Stage("after_cutout_mipped_blocks", RenderType.cutoutMipped());

        /**
         * Use this to render custom block-like geometry into the world, before vanilla renders any.
         */
        public static final Stage BEFORE_CUTOUT_BLOCKS = new Stage("before_cutout_blocks", RenderType.cutout());
        /**
         * Use this to render custom block-like geometry into the world, after vanilla renders any.
         */
        public static final Stage AFTER_CUTOUT_BLOCKS = new Stage("after_cutout_blocks", RenderType.cutout());

        /**
         * Use this to render custom block-like geometry into the world, before vanilla renders any.
         */
        public static final Stage BEFORE_ENTITIES = new Stage("before_entities", null);
        /**
         * Use this to render custom block-like geometry into the world, after vanilla renders any.
         */
        public static final Stage AFTER_ENTITIES = new Stage("after_entities", null);

        /**
         * Use this to render custom block-like geometry into the world, before vanilla renders any.
         */
        public static final Stage BEFORE_BLOCK_ENTITIES = new Stage("before_block_entities", null);
        /**
         * Use this to render custom block-like geometry into the world.
         */
        public static final Stage AFTER_BLOCK_ENTITIES = new Stage("after_block_entities", null);

        /**
         * Use this to render custom block-like geometry into the world, before vanilla renders any.
         * Due to how transparency sorting works, this stage may not work properly with translucency. If you intend to render translucency,
         * try using {@link #AFTER_TRIPWIRE_BLOCKS} or {@link #AFTER_PARTICLES}.
         * Although this is called within a fabulous graphics target, it does not function properly in many cases.
         */
        public static final Stage BEFORE_TRANSLUCENT_BLOCKS = new Stage("before_translucent_blocks", RenderType.translucent());
        /**
         * Use this to render custom block-like geometry into the world, after vanilla renders any.
         * Due to how transparency sorting works, this stage may not work properly with translucency. If you intend to render translucency,
         * try using {@link #AFTER_TRIPWIRE_BLOCKS} or {@link #AFTER_PARTICLES}.
         * Although this is called within a fabulous graphics target, it does not function properly in many cases.
         */
        public static final Stage AFTER_TRANSLUCENT_BLOCKS = new Stage("after_translucent_blocks", RenderType.translucent());

        /**
         * Use this to render custom block-like geometry into the world, before vanilla renders any.
         */
        public static final Stage BEFORE_TRIPWIRE_BLOCKS = new Stage("before_tripwire_blocks", RenderType.tripwire());
        /**
         * Use this to render custom block-like geometry into the world, after vanilla renders any.
         */
        public static final Stage AFTER_TRIPWIRE_BLOCKS = new Stage("after_tripwire_blocks", RenderType.tripwire());

        /**
         * Use this to render custom effects into the world, such as custom entity-like objects or special rendering effects.
         * Called within a fabulous graphics target.
         * Happens after entities render.
         *
         * @see NeoForgeRenderTypes#TRANSLUCENT_ON_PARTICLES_TARGET
         */
        public static final Stage BEFORE_PARTICLES = new Stage("before_particles", null);
        /**
         * Use this to render custom effects into the world, such as custom entity-like objects or special rendering effects.
         * Called within a fabulous graphics target.
         * Happens after entities render.
         *
         * @see NeoForgeRenderTypes#TRANSLUCENT_ON_PARTICLES_TARGET
         */
        public static final Stage AFTER_PARTICLES = new Stage("after_particles", null);

        /**
         * Use this to render custom weather effects into the world, before vanilla renders any.
         * Called within a fabulous graphics target.
         */
        public static final Stage BEFORE_WEATHER = new Stage("before_weather", null);
        /**
         * Use this to render custom weather effects into the world, before vanilla renders any.
         * Called within a fabulous graphics target.
         */
        public static final Stage AFTER_WEATHER = new Stage("after_weather", null);

        /**
         * Use this to render before everything in the level has been rendered.
         * Called before {@link LevelRenderer#renderLevel(float, long, boolean, Camera, GameRenderer, LightTexture, Matrix4f, Matrix4f)} begins rendering geometry.
         */
        public static final Stage BEFORE_LEVEL = new Stage("before_level", null);
        /**
         * Use this to render after everything in the level has been rendered.
         * Called after {@link LevelRenderer#renderLevel(float, long, boolean, Camera, GameRenderer, LightTexture, Matrix4f, Matrix4f)} finishes.
         */
        public static final Stage AFTER_LEVEL = new Stage("after_level", null);

        private static final Map<RenderType, Pair<Stage, Stage>> PAIRS = Stream.of(
                Pair.of(BEFORE_SKY, AFTER_SKY),
                Pair.of(BEFORE_SOLID_BLOCKS, AFTER_SOLID_BLOCKS),
                Pair.of(BEFORE_CUTOUT_MIPPED_BLOCKS_BLOCKS, AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS),
                Pair.of(BEFORE_CUTOUT_BLOCKS, AFTER_CUTOUT_BLOCKS),
                Pair.of(BEFORE_ENTITIES, AFTER_ENTITIES),
                Pair.of(BEFORE_BLOCK_ENTITIES, AFTER_BLOCK_ENTITIES),
                Pair.of(BEFORE_TRANSLUCENT_BLOCKS, AFTER_TRANSLUCENT_BLOCKS),
                Pair.of(BEFORE_TRIPWIRE_BLOCKS, AFTER_TRIPWIRE_BLOCKS),
                Pair.of(BEFORE_PARTICLES, AFTER_PARTICLES),
                Pair.of(BEFORE_WEATHER, AFTER_WEATHER),
                Pair.of(BEFORE_LEVEL, AFTER_LEVEL))
                .filter(pair -> pair.first().renderType != null)
                .collect(Collectors.toMap(
                        pair -> pair.first().renderType,
                        Function.identity()));

        private final String name;
        @Nullable
        private final RenderType renderType;

        private Stage(String name, @Nullable RenderType renderType) {
            this.name = name;
            this.renderType = renderType;
        }

        @Override
        public String toString() {
            return this.name;
        }

        /**
         * {@return the {@linkplain Stage stage} bound to the {@linkplain RenderType render type}, or null if no value is present}
         */
        @Nullable
        @Deprecated(forRemoval = true, since = "21.4")
        public static Stage fromRenderType(RenderType renderType) {
            return stageAfterRenderType(renderType);
        }

        /**
         * {@return the {@linkplain Stage stage} bound to the {@linkplain RenderType render type}, or null if no value is present}
         */
        @Nullable
        public static Stage stageBeforeRenderType(RenderType renderType) {
            var pair = PAIRS.get(renderType);
            if (pair == null)
                return null;

            return pair.first();
        }

        /**
         * {@return the {@linkplain Stage stage} bound to the {@linkplain RenderType render type}, or null if no value is present}
         */
        @Nullable
        public static Stage stageAfterRenderType(RenderType renderType) {
            var pair = PAIRS.get(renderType);
            if (pair == null)
                return null;

            return pair.second();
        }
    }
}

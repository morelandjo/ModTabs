package vodmordia.modtabs.config.lib;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.io.Reader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File-only stand-in for MidnightLib's {@code eu.midnightdust.lib.config.MidnightConfig}.
 * MidnightLib has no 1.18.2 Forge build, so on this target the config has no in-game
 * MidnightLib screen; it is read from and written to {@code config/<modid>.json} only.
 *
 * <p>The on-disk format matches MidnightLib 1.4.x exactly so existing config files stay
 * compatible: a pretty-printed JSON object of every public static {@link Entry} field
 * (field name -> value, enums by constant name). Fields without {@link Entry} (e.g.
 * {@link Comment} placeholders) are never written; transient/private fields are ignored.
 *
 * <p>Only the subset of the MidnightLib API that ModTabs uses is provided: the annotations
 * (attributes kept for source compatibility, otherwise unused), {@link #init}, {@link #write}
 * and {@link #configClass}.
 */
public abstract class MidnightConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** modid -> registered config class, mirroring MidnightLib's public map. */
    public static final Map<String, Class<?>> configClass = new ConcurrentHashMap<>();

    private static final Gson GSON = new GsonBuilder()
            // Overrides Gson's default (TRANSIENT | STATIC) so static fields are (de)serialised.
            .excludeFieldsWithModifiers(Modifier.TRANSIENT)
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .addSerializationExclusionStrategy(new ExclusionStrategy() {
                @Override public boolean shouldSkipField(FieldAttributes f) { return f.getAnnotation(Entry.class) == null; }
                @Override public boolean shouldSkipClass(Class<?> clazz) { return false; }
            })
            .setPrettyPrinting()
            .create();

    private static Path path(String modid) {
        return FMLPaths.CONFIGDIR.get().resolve(modid + ".json");
    }

    /**
     * Registers {@code config} under {@code modid} and loads {@code config/<modid>.json} into
     * its static fields. If the file is missing or unreadable, the current (default) values
     * are written out instead — same behaviour as MidnightLib.
     */
    public static void init(String modid, Class<?> config) {
        configClass.put(modid, config);
        Path path = path(modid);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            GSON.fromJson(reader, config);
        } catch (Exception e) {
            write(modid);
        }
    }

    /** Serialises the static {@link Entry} fields of the class registered for {@code modid}. */
    public static void write(String modid) {
        Class<?> config = configClass.get(modid);
        if (config == null) {
            LOGGER.warn("MidnightConfig.write called for unregistered mod id '{}'", modid);
            return;
        }
        Path path = path(modid);
        try {
            Files.createDirectories(path.getParent());
            Object instance = config.getDeclaredConstructor().newInstance();
            Files.writeString(path, GSON.toJson(instance), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("Failed to write config {}", path, e);
        }
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    public @interface Entry {
        int width() default 400;
        double min() default Double.MIN_NORMAL;
        double max() default Double.MAX_VALUE;
        String name() default "";
        boolean isColor() default false;
        boolean isSlider() default false;
        int precision() default 100;
        String category() default "default";
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    public @interface Comment {
        boolean centered() default false;
        String category() default "default";
    }

    /** Hidden from the (absent) config screen; still persisted when also annotated {@link Entry}. */
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    public @interface Hidden {}

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    public @interface Client {}

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    public @interface Server {}
}

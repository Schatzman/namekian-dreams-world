package io.github.namekiandreams.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public final class NamekianBiomeSource extends BiomeSource {
    public static final Codec<NamekianBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().fieldOf("biomes").forGetter(NamekianBiomeSource::entries)
    ).apply(instance, NamekianBiomeSource::new));

    private final List<Entry> entries;
    private final int[] weights;

    public NamekianBiomeSource(List<Entry> entries) {
        if (entries.isEmpty()) throw new IllegalArgumentException("Namekian biome source requires at least one biome");
        this.entries = List.copyOf(entries);
        this.weights = entries.stream().mapToInt(Entry::weight).toArray();
    }

    public List<Entry> entries() { return entries; }

    @Override
    protected Codec<? extends BiomeSource> codec() { return CODEC; }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() { return entries.stream().map(Entry::biome); }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        int blockX = quartX << 2;
        int blockY = quartY << 2;
        int blockZ = quartZ << 2;
        ResourceKey<Biome> preferred = NamekianBiomeClassifier.selectBiomeKey(blockX, blockY, blockZ);
        Optional<Holder<Biome>> holder = findHolder(preferred);
        if (holder.isPresent()) return holder.get();
        return entries.get(NamekianBiomeClassifier.selectWeightedIndex(blockX, blockY, blockZ, weights)).biome();
    }

    private Optional<Holder<Biome>> findHolder(ResourceKey<Biome> key) {
        return entries.stream()
                .map(Entry::biome)
                .filter(holder -> holder.is(key))
                .findFirst();
    }

    public record Entry(Holder<Biome> biome, int weight) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Biome.CODEC.fieldOf("biome").forGetter(Entry::biome),
                Codec.INT.optionalFieldOf("weight", 1).forGetter(Entry::weight)
        ).apply(instance, Entry::new));

        public Entry {
            if (weight <= 0) throw new IllegalArgumentException("biome weight must be positive");
        }
    }
}

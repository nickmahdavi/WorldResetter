package me.nkightly.worldresetter;

import java.util.Random;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class LimboGenerator extends ChunkGenerator {
    @Override
    public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
        return createChunkData(world);
    }
}
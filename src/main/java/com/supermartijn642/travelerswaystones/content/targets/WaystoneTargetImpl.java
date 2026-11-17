package com.supermartijn642.travelerswaystones.content.targets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.supermartijn642.travelerswaystones.api.targets.WaystoneTarget;
import com.supermartijn642.travelerswaystones.content.WaystoneEventsImpl;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public final class WaystoneTargetImpl implements WaystoneTarget {

    public static JsonObject serialize(WaystoneTarget target, RegistryAccess registryAccess){
        JsonObject json = new JsonObject();
        json.addProperty("id", target.id());
        json.addProperty("name", target.name());
        json.addProperty("ownerLower", target.owner().getLeastSignificantBits());
        json.addProperty("ownerHigher", target.owner().getMostSignificantBits());
        json.addProperty("dimension", target.dimension().identifier().toShortString());
        json.addProperty("biome", target.biome().identifier().toShortString());
        json.addProperty("pos", target.pos().asLong());
        RegistryOps<JsonElement> ops = registryAccess.createSerializationContext(JsonOps.INSTANCE);
        json.add("block", BlockState.CODEC.encodeStart(ops, target.block()).getOrThrow());
        JsonArray discoverers = new JsonArray(target.discoverers().size() * 2);
        for(UUID discoverer : target.discoverers()){
            discoverers.add(discoverer.getMostSignificantBits());
            discoverers.add(discoverer.getLeastSignificantBits());
        }
        json.add("discoverers", discoverers);
        return json;
    }

    public static WaystoneTarget deserialize(JsonObject json, RegistryAccess registryAccess){
        RegistryOps<JsonElement> ops = registryAccess.createSerializationContext(JsonOps.INSTANCE);
        WaystoneTargetImpl target = new WaystoneTargetImpl(
            false,
            json.get("id").getAsInt(),
            json.get("name").getAsString(),
            new UUID(json.get("ownerHigher").getAsLong(), json.get("ownerLower").getAsLong()),
            ResourceKey.create(Registries.DIMENSION, Identifier.parse(json.get("dimension").getAsString())),
            ResourceKey.create(Registries.BIOME, Identifier.parse(json.get("biome").getAsString())),
            BlockPos.of(json.get("pos").getAsLong()),
            BlockState.CODEC.decode(ops, json.get("block")).getOrThrow().getFirst());
        JsonArray discoverers = json.getAsJsonArray("discoverers");
        for(int i = 0; i + 1 < discoverers.size(); i += 2)
            target.discoverers.add(new UUID(discoverers.get(i).getAsLong(), discoverers.get(i + 1).getAsLong()));
        return target;
    }

    public static boolean hasMissingBlock(JsonObject json){
        Identifier block = Identifier.tryParse(blockName(json));
        return block != null && !BuiltInRegistries.BLOCK.containsKey(block);
    }

    public static String blockName(JsonObject json){
        JsonElement block = json.get("block");
        return block.isJsonPrimitive() ? block.getAsString() : block.getAsJsonObject().get("id").getAsString();
    }

    public static void encode(WaystoneTarget target, FriendlyByteBuf buffer){
        buffer.writeInt(target.id());
        buffer.writeUtf(target.name());
        buffer.writeUUID(target.owner());
        buffer.writeResourceKey(target.dimension());
        buffer.writeResourceKey(target.biome());
        buffer.writeBlockPos(target.pos());
        buffer.writeVarInt(Block.getId(target.block()));
    }

    public static WaystoneTarget decode(FriendlyByteBuf buffer){
        return new WaystoneTargetImpl(
            true,
            buffer.readInt(),
            buffer.readUtf(WaystoneTarget.MAX_NAME_LENGTH),
            buffer.readUUID(),
            buffer.readResourceKey(Registries.DIMENSION),
            buffer.readResourceKey(Registries.BIOME),
            buffer.readBlockPos(),
            Block.stateById(buffer.readVarInt())
        );
    }

    private final boolean isClient;
    private final int id;
    private String name;
    private final UUID owner;
    private final ResourceKey<Level> dimension;
    private ResourceKey<Biome> biome;
    private BlockPos pos;
    private ChunkPos chunkPos;
    private BlockState block;
    private final Set<UUID> discoverers = new ObjectOpenHashSet<>(), discoverersView = Collections.unmodifiableSet(this.discoverers);
    private boolean valid = true;
    /**
     * Flag used when moving a waystone to prevent the target from being removed.
     */
    private boolean preventRemoveWhileMoving = false;

    public WaystoneTargetImpl(boolean isClient, int id, String name, UUID owner, ResourceKey<Level> dimension, ResourceKey<Biome> biome, BlockPos pos, BlockState block){
        this.isClient = isClient;
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.dimension = dimension;
        this.biome = biome;
        this.pos = pos;
        this.chunkPos = ChunkPos.containing(pos);
        this.block = block;
    }

    public void setName(String name){
        String oldName = this.name;
        this.name = name;
        if(this.isClient)
            WaystoneEventsImpl.Rename.invoke(this, null, oldName, true);
    }

    public void setBiomePosAndBlock(ResourceKey<Biome> biome, BlockPos pos, BlockState block){
        this.biome = biome;
        this.pos = pos;
        this.chunkPos = ChunkPos.containing(pos);
        this.block = block;
    }

    public void addDiscoverer(UUID discoverer){
        this.discoverers.add(discoverer);
    }

    public void removeDiscoverer(UUID discoverer){
        this.discoverers.remove(discoverer);
    }

    public boolean isValid(){
        return this.valid;
    }

    public void invalidate(){
        this.valid = false;
    }

    public boolean isPreventRemoveWhileMoving(){
        return this.preventRemoveWhileMoving;
    }

    public void setPreventRemoveWhileMoving(boolean preventRemoveWhileMoving){
        this.preventRemoveWhileMoving = preventRemoveWhileMoving;
    }

    @Override
    public int id(){
        return this.id;
    }

    @Override
    public String name(){
        return this.name;
    }

    @Override
    public UUID owner(){
        return this.owner;
    }

    @Override
    public ResourceKey<Level> dimension(){
        return this.dimension;
    }

    @Override
    public ResourceKey<Biome> biome(){
        return this.biome;
    }

    @Override
    public BlockPos pos(){
        return this.pos;
    }

    public ChunkPos chunkPos(){
        return this.chunkPos;
    }

    @Override
    public BlockState block(){
        return this.block;
    }

    @Override
    public Set<UUID> discoverers(){
        if(this.isClient)
            throw new IllegalStateException("Cannot get discoverers on the client!");
        return this.discoverersView;
    }

    @Override
    public boolean equals(Object o){
        if(o == null || this.getClass() != o.getClass()) return false;

        WaystoneTargetImpl that = (WaystoneTargetImpl)o;
        return this.id == that.id;
    }

    @Override
    public int hashCode(){
        return this.id;
    }
}

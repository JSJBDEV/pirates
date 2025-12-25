package ace.actually.pirates.blocks.entity;

import ace.actually.pirates.Pirates;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ShipIdBlockEntity extends BlockEntity {
    private String shipName = "";
    public ShipIdBlockEntity(BlockPos pos, BlockState state) {
        super(Pirates.SHIP_ID_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putString("shipName", shipName);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        shipName=nbt.getString("shipName");
    }

    public void setShipName(String shipName) {
        this.shipName = shipName;
        setChanged();
    }

    public String getShipName() {
        return shipName;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

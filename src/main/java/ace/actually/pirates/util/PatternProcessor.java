package ace.actually.pirates.util;

import ace.actually.pirates.blocks.entity.MotionInvokingBlockEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.apache.commons.io.FileUtils;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.mod.api.SeatedControllingPlayer;
import org.valkyrienskies.mod.common.util.GameTickForceApplier;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PatternProcessor {


    /**
     * A pattern is any combination of impulses to supply to the MotionInvokingBlock
     * forward 1 0
     * left 0.05 10
     * this is the equivalent to, impulse forward and left at the same time, then wait 10 ticks
     */

    public static NbtList loadPattern(String fileLoc)
    {
        NbtList list = null;
        File file = new File(FabricLoader.getInstance().getConfigDirectory().getPath() + "/pirates/patterns/"+fileLoc);
        try {
            List<String> lines = FileUtils.readLines(file,"utf-8");
            list=new NbtList();
            for(String line: lines)
            {
                list.add(NbtString.of(line));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public static void setupBasicPatterns()
    {
        File file = new File(FabricLoader.getInstance().getConfigDirectory().getPath() + "/pirates/patterns/circle.pattern");
        if(!file.exists())
        {
            try {
                FileUtils.writeLines(file, Arrays.asList("forward 1 1","right 0.05 10"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        file = new File(FabricLoader.getInstance().getConfigDirectory().getPath() + "/pirates/patterns/rcircle.pattern");
        if(!file.exists())
        {
            try {
                FileUtils.writeLines(file, Arrays.asList("forward 1 1","left 0.05 10"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }




    /**
     * @deprecated ship patterns are planned for removal, will be replaced with proper ship ai
     **/
    @Deprecated
    public static void utiliseInternalPattern(SeatedControllingPlayer seatedControllingPlayer, MotionInvokingBlockEntity be) {
        String[] instruction = be.getInstructions().getString(0).split(" ");

        if (seatedControllingPlayer == null) return;
        switch (instruction[0]) {
            case "forward" -> seatedControllingPlayer.setForwardImpulse(Float.parseFloat(instruction[1]));
            case "left" -> seatedControllingPlayer.setLeftImpulse(Float.parseFloat(instruction[1]));
            case "right" -> seatedControllingPlayer.setLeftImpulse(-Float.parseFloat(instruction[1]));
            case "backwards" -> seatedControllingPlayer.setForwardImpulse(-Float.parseFloat(instruction[1]));
            case "up" -> seatedControllingPlayer.setUpImpulse(Float.parseFloat(instruction[1]));
            case "down" -> seatedControllingPlayer.setUpImpulse(-Float.parseFloat(instruction[1]));
        }

        be.setNextInstruction(Objects.requireNonNull(be.getWorld()).getTime() + Long.parseLong(instruction[2]));
        be.advanceInstructionList();
        be.markDirty();
    }

}

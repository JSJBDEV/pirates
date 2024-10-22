package ace.actually.pirates.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

}

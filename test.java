
// Package name here

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class TestGenerator extends WorldGenerator {

    @Override
    public boolean generate(World world, Random random, int x, int y, int z) {

        this.setBlockAndNotifyAdequately(world, x + 0, y + 0, z + 0, Block.getBlockById("5"), 1);
        this.setBlockAndNotifyAdequately(world, x + 0, y + 0, z + 1, Block.getBlockById("5"), 1);
        this.setBlockAndNotifyAdequately(world, x + 1, y + 0, z + 0, Block.getBlockById("5"), 1);
        this.setBlockAndNotifyAdequately(world, x + 1, y + 0, z + 1, Block.getBlockById("5"), 1);
        this.setBlockAndNotifyAdequately(world, x + 0, y + 1, z + 0, Block.getBlockById("54"), 3);
        this.setBlockAndNotifyAdequately(world, x + 0, y + 1, z + 1, Block.getBlockById("18"), 4);
        this.setBlockAndNotifyAdequately(world, x + 1, y + 1, z + 0, Block.getBlockById("1292"), 0);
        this.setBlockAndNotifyAdequately(world, x + 1, y + 1, z + 1, Block.getBlockById("1292"), 0);

        NBTTagList TileEntities1 = new NBTTagList();

        NBTTagCompound tagCompound1 = new NBTTagCompound();
        NBTTagList Items2 = new NBTTagList();
        NBTTagCompound tagCompound2 = new NBTTagCompound();
        tagCompound2.setShort("id", (short)44);
        tagCompound2.setShort("Damage", (short)1);
        tagCompound2.setByte("Count", (byte)1);
        tagCompound2.setByte("Slot", (byte)10);
        Items2.appendTag((NBTTagCompound)tagCompound2);
        NBTTagCompound tagCompound3 = new NBTTagCompound();
        tagCompound3.setShort("id", (short)116);
        tagCompound3.setShort("Damage", (short)0);
        tagCompound3.setByte("Count", (byte)1);
        tagCompound3.setByte("Slot", (byte)12);
        Items2.appendTag((NBTTagCompound)tagCompound3);
        NBTTagCompound tagCompound4 = new NBTTagCompound();
        tagCompound4.setShort("id", (short)45);
        tagCompound4.setShort("Damage", (short)0);
        tagCompound4.setByte("Count", (byte)1);
        tagCompound4.setByte("Slot", (byte)14);
        Items2.appendTag((NBTTagCompound)tagCompound4);

        tagCompound1.setTag("Items", Items2);
        tagCompound1.setString("id", "Chest");
        tagCompound1.setInteger("z", (int)(z + 0));
        tagCompound1.setInteger("y", (int)(y + 1));
        tagCompound1.setInteger("x", (int)(x + 0));
        TileEntities1.appendTag((NBTTagCompound)tagCompound1);
        world.getTileEntity(x + 0, y + 1, z + 0).readFromNBT(tagCompound1);


        return true;
    }
}

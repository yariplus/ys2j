package com.yaricraft;

import org.jnbt.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Main
{
    public static PrintWriter generatorStream;
    public static PrintWriter worldStream;
    public static int tagCounter = 0;
    public static int listCounter = 0;
    public static int teX, teY, teZ;
    
    public static void main(String[] args)
    {
        // Read the block.csv and item.csv
        BufferedReader bufferedReader;
        boolean useBlockNames = true;
        Map<Integer, String> blockNames = new HashMap<Integer, String>();

        try
        {
            String l;
            bufferedReader = new BufferedReader(new FileReader("block.csv"));
            bufferedReader.readLine();
            while ((l = bufferedReader.readLine()) != null)
            {
                String[] line = l.split(",");
                blockNames.put(Integer.parseInt(line[1]),line[0]);
            }
        } catch (FileNotFoundException e1)
        {
            e1.printStackTrace();
            useBlockNames = false;
        } catch (IOException e)
        {
            e.printStackTrace();
            useBlockNames = false;
        }

        if (args.length == 0) args = new String[]{"test.schematic"};

        for (String schematicPath: args)
        {
            // Make sure it's a schematic
            String[] schematicPathTokens = schematicPath.split("[/\\\\]");
            String schematicFile = schematicPathTokens[schematicPathTokens.length-1].split("\"")[0];
            String schematicName = schematicFile.split("\\.")[0];
            System.out.println("Reading " + schematicFile);
            if (!schematicFile.split("\\.")[1].equals("schematic"))
            {
                System.out.println("Reading failed. Not a .schematic file.");
                continue;
            }

            // Read the schematic
            FileInputStream schemInputStream;
            NBTInputStream inputStream;
            CompoundTag compoundTag;

            try
            {
                schemInputStream = new FileInputStream(schematicPath);
                inputStream = new NBTInputStream(schemInputStream);
                compoundTag = (CompoundTag)inputStream.readTag();
            } catch (FileNotFoundException e) { e.printStackTrace(); continue;
            } catch (IOException e) { e.printStackTrace(); continue; }

            Map<String, Tag> tagMap = compoundTag.getValue();

            byte[] bytesBlock    = ((ByteArrayTag)tagMap.get("Blocks")).getValue();
            byte[] bytesAddBlock = ((ByteArrayTag)tagMap.get("AddBlocks")).getValue();
            byte[] bytesMeta     = ((ByteArrayTag)tagMap.get("Data")).getValue();

            int height = ((ShortTag)tagMap.get("Height")).getValue();
            int width  = ((ShortTag)tagMap.get("Width")).getValue();
            int length = ((ShortTag)tagMap.get("Length")).getValue();

            int[] blocks = new int[bytesBlock.length];

            for (int i = 0; i < bytesBlock.length; i++)
            {
                if ((i >> 1) >= bytesAddBlock.length)
                {
                    blocks[i] = (short) (bytesBlock[i] & 0xFF);
                } else {
                    if ((i & 1) == 0)
                    {
                        blocks[i] = (short) (((bytesAddBlock[i >> 1] & 0x0F) << 8) + (bytesBlock[i] & 0xFF));
                    } else {
                        blocks[i] = (short) (((bytesAddBlock[i >> 1] & 0xF0) << 4) + (bytesBlock[i] & 0xFF));
                    }
                }
            }

            //
            String ss = "";
            for (byte s: bytesAddBlock) ss = ss + " " + s;
            System.out.println(ss);
            String ii = "";
            for (byte i: bytesBlock) ii = ii + " " + i;
            System.out.println(ii);

            // Write the schematic
            try
            {
                generatorStream = new PrintWriter(new FileWriter(schematicPath.split("\\.")[0] + ".java"));
                worldStream = new PrintWriter(new FileWriter(schematicPath.split("\\.")[0] + ".txt"));
            } catch (IOException e)
            { e.printStackTrace(); continue; }

            generatorStream.println();
            generatorStream.println("// Package name here");
            generatorStream.println();
            generatorStream.println("import net.minecraft.nbt.NBTTagCompound;");
            generatorStream.println("import net.minecraft.nbt.NBTTagList;");
            generatorStream.println("import net.minecraft.block.Block;");
            generatorStream.println("import net.minecraft.world.World;");
            generatorStream.println("import net.minecraft.world.gen.feature.WorldGenerator;");
            generatorStream.println();
            generatorStream.println("import java.util.Random;");
            generatorStream.println();
            generatorStream.println("public class " +schematicName.substring(0,1).toUpperCase()+schematicName.substring(1)+ "Generator extends WorldGenerator {");
            generatorStream.println();
            generatorStream.println("    @Override");
            generatorStream.println("    public boolean generate(World world, Random random, int x, int y, int z) {");
            generatorStream.println();

            String blockCode = "";

            for (int h = 0; h < height; h++)
            {
                for (int w = 0; w < width; w++)
                {
                    for (int l = 0; l < length; l++)
                    {
                        int i = (h*(blocks.length/height)) + (w*((blocks.length/height)/width)) + l;
                        int id = blocks[i];
                        int meta = bytesMeta[i];

                        if (useBlockNames)
                        {
                            if (blockNames.get(id) == null)
                            {
                                blockCode = "Block.getBlockFromName(\"minecraft:air\")";
                            }else{
                                blockCode = "Block.getBlockFromName(\"" +blockNames.get(id)+ "\")";
                            }
                        }else{
                            blockCode = "Block.getBlockById(\"" +id+ "\")";
                        }

                        generatorStream.println("        this.setBlockAndNotifyAdequately(world, x + "+w+", y + "+h+", z + "+l+", "+blockCode+", "+meta+");");
                        worldStream.println("        world.setBlock(x + " + w + ", y + " + h + ", z + " + l + ", " + blockCode + ", " + meta + ", 3);");
                    }
                }
            }

            // Read/Write TEs
            writeList(((ListTag)tagMap.get("TileEntities")), true);

            generatorStream.println("        return true;");
            generatorStream.println("    }");
            generatorStream.println("}");

            try
            {
                if (schemInputStream != null) schemInputStream.close();
                if (generatorStream != null) generatorStream.close();
                if (worldStream != null) worldStream.close();
            } catch (IOException e) { e.printStackTrace(); continue; }
        }
    }
    
    private static void writeTag(Tag tag, String tagCompoundName)
    {
        if(tag.getClass().equals(ByteArrayTag.class)) {
            writeToStreams("        " + tagCompoundName + ".setByteArray(\"" + tag.getName() + "\", (byte[])" + tag.getValue() + ");");
        } else if(tag.getClass().equals(ByteTag.class)) {
            writeToStreams("        " + tagCompoundName + ".setByte(\"" + tag.getName() + "\", (byte)" + tag.getValue() + ");");
        } else if(tag.getClass().equals(CompoundTag.class)) {
            String tagEmbeddedName = "tagEmbeddedCompound" + ++tagCounter;
            writeToStreams("        NBTTagCompound " + tagCompoundName + " = new NBTTagCompound();");
            for (Map.Entry<String, Tag> entry : ((CompoundTag)tag).getValue().entrySet())
            {
                Tag tagEmbedded = entry.getValue();
                writeTag(tagEmbedded, tagEmbeddedName);
            }
            writeToStreams("        " + tagCompoundName + ".setTag(\"" +tag.getName()+ "\", " +tagEmbeddedName+ ");");
        } else if(tag.getClass().equals(DoubleTag.class)) {
            writeToStreams("        " + tagCompoundName + ".setDouble(\"" +tag.getName()+ "\", (double)" + tag.getValue() + ");");
        } else if(tag.getClass().equals(FloatTag.class)) {
            writeToStreams("        " + tagCompoundName + ".setFloat(\"" +tag.getName()+ "\", (float)" + tag.getValue() + ");");
        } else if(tag.getClass().equals(IntTag.class)) {
            String coord = "";
            if (tag.getName().equals("x")) { coord = "x + "; teX = Integer.parseInt(tag.getValue().toString()); }
            if (tag.getName().equals("y")) { coord = "y + "; teY = Integer.parseInt(tag.getValue().toString()); }
            if (tag.getName().equals("z")) { coord = "z + "; teZ = Integer.parseInt(tag.getValue().toString()); }
            writeToStreams("        " + tagCompoundName + ".setInteger(\"" + tag.getName() + "\", (int)(" + coord + tag.getValue() + "));");
        } else if(tag.getClass().equals(ListTag.class)) {
            String listName = writeList((ListTag)tag, false);
            writeToStreams("        " + tagCompoundName + ".setTag(\"" + tag.getName() + "\", " + listName + ");");
        } else if(tag.getClass().equals(LongTag.class)) {
            writeToStreams("        " + tagCompoundName + ".setLong(\"" + tag.getName() + "\", (long)" + tag.getValue() + ");");
        } else if(tag.getClass().equals(ShortTag.class)) {
            writeToStreams("        " + tagCompoundName + ".setShort(\"" + tag.getName() + "\", (short)" + tag.getValue() + ");");
        } else if(tag.getClass().equals(StringTag.class)) {
            writeToStreams("        " + tagCompoundName + ".setString(\"" + tag.getName() + "\", \"" + tag.getValue() + "\");");
        }
    }

    private static void writeCompound(Tag tagCompound, String listName, boolean isTE)
    {
        if (isTE) writeToStreams("");

        String tagCompoundName = "tagCompound" + ++tagCounter;
        writeToStreams("        NBTTagCompound " + tagCompoundName + " = new NBTTagCompound();");

        for (Map.Entry<String, Tag> entry : ((CompoundTag)tagCompound).getValue().entrySet())
        {
            Tag tag = entry.getValue();
            writeTag(tag, tagCompoundName);
        }

        writeToStreams("        " + listName + ".appendTag((NBTTagCompound)" + tagCompoundName + ");");

        if (isTE) writeToStreams("        world.getTileEntity(x + " + teX + ", y + " + teY + ", z + " + teZ + ").readFromNBT(" + tagCompoundName + ");");
        if (isTE) writeToStreams("");
    }

    private static String writeList(ListTag listTag, boolean isTE)
    {
        if (isTE) writeToStreams("");
        String listName = listTag.getName() + ++listCounter;
        writeToStreams("        NBTTagList " + listName + " = new NBTTagList();");

        for (Tag tagCompound: listTag.getValue())
        {
            writeCompound(tagCompound, listName, isTE);
        }

        writeToStreams("");
        return listName;
    }
    
    private static void writeToStreams(String line)
    {
        generatorStream.println(line);
        worldStream.println(line);
    }
}

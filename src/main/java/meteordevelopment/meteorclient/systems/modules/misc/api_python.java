package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixin.AbstractSignEditScreenAccessor;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import meteordevelopment.meteorclient.events.world.TickEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import net.minecraft.util.math.BlockPos;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;



public class api_python extends Module {
    private String[] text;
    private BlockPos breakingBlockPos = null;
    private int breakingTicks = 0;
    private final int MAX_BREAK_TICKS = 40; // approx 2 seconds of breaking

    public api_python() {
        super(Categories.Misc, "api_python", "a python api");
    }


    @Override
    public void onDeactivate() {
        text = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {

        File folder = new File("api");
        if (!folder.exists()) folder.mkdir();
        try {
        File blockFile = new File("api/placeblock.txt");
            if (blockFile.exists()) {
                String coords = new String(java.nio.file.Files.readAllBytes(blockFile.toPath())).trim();
                if (!coords.isEmpty()) {
                    String[] parts = coords.split(",");
                    if (parts.length == 3) {
                        int x = Integer.parseInt(parts[0].trim());
                        int y = Integer.parseInt(parts[1].trim());
                        int z = Integer.parseInt(parts[2].trim());

                        BlockPos pos = new BlockPos(x, y, z);

                        if (mc.world.getBlockState(pos).isAir()) {
                            if (mc.player.getMainHandStack().getItem() instanceof BlockItem) {

                                BlockPos placeOn = pos.down();
                                Direction face = Direction.UP;

                                BlockHitResult hitResult = new BlockHitResult(
                                    Vec3d.ofCenter(placeOn),
                                    face,
                                    placeOn,
                                    false
                                );

                                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
                                mc.player.swingHand(Hand.MAIN_HAND);


                                java.nio.file.Files.write(blockFile.toPath(), new byte[0]);
                            }
                        }
                    }
                }
            }
    } catch (IOException | NumberFormatException e) {
        e.printStackTrace();
    }

    if (breakingBlockPos != null) {

        if (mc.world.getBlockState(breakingBlockPos).isAir() || breakingTicks >= MAX_BREAK_TICKS) {
            mc.interactionManager.cancelBlockBreaking();
            breakingBlockPos = null;
            breakingTicks = 0;
        } else {

            mc.interactionManager.updateBlockBreakingProgress(breakingBlockPos, Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);
            breakingTicks++;
        }
    } else {
        try {
            File breakFile = new File("api/breakblock.txt");
            if (breakFile.exists()) {
                String coords = new String(Files.readAllBytes(breakFile.toPath())).trim();
                if (!coords.isEmpty()) {
                    String[] parts = coords.split(",");
                    if (parts.length == 3) {
                        int x = Integer.parseInt(parts[0].trim());
                        int y = Integer.parseInt(parts[1].trim());
                        int z = Integer.parseInt(parts[2].trim());

                        BlockPos pos = new BlockPos(x, y, z);
                        if (!mc.world.getBlockState(pos).isAir()) {
                            breakingBlockPos = pos;
                            breakingTicks = 0;

                            mc.interactionManager.updateBlockBreakingProgress(breakingBlockPos, Direction.UP);
                            mc.player.swingHand(Hand.MAIN_HAND);

                            Files.write(breakFile.toPath(), new byte[0]);
                        }
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }




     /*
         to      chat
              |
              V
    */
    if (mc.player == null || mc.world == null) return;
    try {
        File file = new File("api/tochat.txt");
        if (file.exists()) {
            String message = new String(java.nio.file.Files.readAllBytes(file.toPath())).trim();
            if (!message.isEmpty()) {
                mc.player.networkHandler.sendChatMessage(message);
                java.nio.file.Files.write(file.toPath(), new byte[0]);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    @EventHandler
    private void onChatPacket(PacketEvent.Receive event) {
         /*
              last  chat
                   |
                   V
         */
        if (event.packet instanceof GameMessageS2CPacket packet) {
            String message = packet.content().getString();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("api/lastchat.txt", false))) {
                writer.write(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

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



public class api_python extends Module {
    private String[] text;

    public api_python() {
        super(Categories.Misc, "api_python", "a python api");
    }


    @Override
    public void onDeactivate() {
        text = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
    }

    @EventHandler
    private void onChatPacket(PacketEvent.Receive event) {
        if (event.packet instanceof GameMessageS2CPacket packet) {
            String message = packet.content().getString();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("lastchat.txt", false))) {
                writer.write(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

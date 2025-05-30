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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import net.minecraft.util.math.BlockPos;

public class Projects_Ace extends Module {
    private String[] text;

    public Projects_Ace() {
        super(Categories.World, "Project_Ace", "Automatically writes signs");
    }

    @Override
    public void onDeactivate() {
        text = null;
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!(event.screen instanceof AbstractSignEditScreen)) return;
        SignBlockEntity sign = ((AbstractSignEditScreenAccessor) event.screen).getSign();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String today = LocalDate.now().format(formatter);
        String username = mc.getSession().getUsername();
        text = new String[] {
            "hydro_null",
            today,
            username,
            sign.getPos().toShortString()
        };
        mc.player.networkHandler.sendPacket(
            new UpdateSignC2SPacket(sign.getPos(), true, text[0], text[1], text[2], text[3])
        );
        String logEntry = String.format(sign.getPos().toShortString() + "\n");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("sign-log.txt", true))) {
            writer.write(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
        event.cancel();
    }
}

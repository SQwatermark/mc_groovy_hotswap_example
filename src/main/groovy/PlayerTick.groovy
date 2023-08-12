import net.minecraft.network.chat.Component
import net.minecraftforge.event.TickEvent

// 临时变量，当脚本被重载之后，这些变量也会重置，如需持久化保存变量，应当利用入参
class TempVariables {
    static int count
}

static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if (event.player.tickCount % 20 == 0) {
        event.player.sendSystemMessage(Component.literal("Groovy" + TempVariables.count ++));
    }
}
package moe.sqwatermark.groovy_test;

import groovy.lang.Script;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("groovy_test")
public class GroovyTest {

    public GroovyTest() {
        GroovyManager.init();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    static class ForgeEvents {
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.side.isServer() && event.phase == TickEvent.PlayerTickEvent.Phase.END) {
                GroovyManager.invoke("PlayerTick.groovy", "onPlayerTick", event);
            }
        }
    }


}

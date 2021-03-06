package delta.mixin.mixins;

import delta.DeltaCore;
import delta.event.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void sendPacket(Packet<?> packet, CallbackInfo ci) {
        PacketEvent packetEvent = new PacketEvent.Send(packet);
        DeltaCore.EVENT_BUS.post(packetEvent);
        if (packetEvent.isCancelled()) ci.cancel();
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void recievePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        PacketEvent packetEvent = new PacketEvent.Receive(packet);
        DeltaCore.EVENT_BUS.post(packetEvent);
        if (packetEvent.isCancelled()) ci.cancel();
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("TAIL"), cancellable = true)
    private void postSendPacket(Packet<?> packet, CallbackInfo ci) {
        PacketEvent packetEvent = new PacketEvent.PostSend(packet);
        DeltaCore.EVENT_BUS.post(packetEvent);
        if (packetEvent.isCancelled()) ci.cancel();
    }

    @Inject(method = "channelRead0", at = @At("TAIL"), cancellable = true)
    private void postRecievePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        PacketEvent packetEvent = new PacketEvent.PostReceive(packet);
        DeltaCore.EVENT_BUS.post(packetEvent);
        if (packetEvent.isCancelled()) ci.cancel();
    }
}

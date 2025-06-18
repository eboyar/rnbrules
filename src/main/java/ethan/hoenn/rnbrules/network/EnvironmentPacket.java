package ethan.hoenn.rnbrules.network;

import ethan.hoenn.rnbrules.environment.client.ClientEnvironmentController;
import ethan.hoenn.rnbrules.utils.enums.Environment;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class EnvironmentPacket {

	private final String environmentId;
	private final float intensity;

	public EnvironmentPacket(String environmentId) {
		this(environmentId, 1.0f);
	}

	public EnvironmentPacket(String environmentId, float intensity) {
		this.environmentId = environmentId;
		this.intensity = Math.max(0.0f, Math.min(1.0f, intensity));
	}

	public static void encode(EnvironmentPacket packet, PacketBuffer buffer) {
		buffer.writeUtf(packet.environmentId);
		buffer.writeFloat(packet.intensity);
	}

	public static EnvironmentPacket decode(PacketBuffer buffer) {
		String envId = buffer.readUtf(32767);
		float intensity = buffer.readFloat();
		return new EnvironmentPacket(envId, intensity);
	}

	public static void handle(EnvironmentPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet));
		});
		context.setPacketHandled(true);
	}

	private static void handleClient(EnvironmentPacket packet) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null) return;

		Environment environment = Environment.fromString(packet.environmentId);
		if (environment == null) environment = Environment.NONE;

		ClientEnvironmentController.getInstance().setClientEnvironment(environment, packet.intensity);
	}
}

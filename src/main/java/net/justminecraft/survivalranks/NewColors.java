package net.justminecraft.survivalranks;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.protocol.ProtocolRegistry;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.ClientboundPackets1_15;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import us.myles.viaversion.libs.gson.JsonArray;
import us.myles.viaversion.libs.gson.JsonElement;
import us.myles.viaversion.libs.gson.JsonObject;
import us.myles.viaversion.libs.gson.JsonPrimitive;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewColors {

    public NewColors() {
        Protocol protocol = ProtocolRegistry.getProtocol(Protocol1_16To1_15_2.class);

        try {
            Field outgoing = Protocol.class.getDeclaredField("outgoing");
            outgoing.setAccessible(true);
            Map<Protocol.Packet, Protocol.ProtocolPacket> map = (Map<Protocol.Packet, Protocol.ProtocolPacket>) outgoing.get(protocol);
            Protocol.Packet packet = new Protocol.Packet(State.PLAY, ClientboundPackets1_15.CHAT_MESSAGE.ordinal());
            Protocol.ProtocolPacket oldPacket = map.get(packet);
            Protocol.ProtocolPacket newPacket = new Protocol.ProtocolPacket(oldPacket.getState(), oldPacket.getOldID(), oldPacket.getNewID(), new PacketRemapper() {
                @Override
                public void registerMap() {
                    handler(wrapper -> {
                        JsonElement json = wrapper.get(Type.COMPONENT, 0);

                        recursiveMatchRank(json);
                    });
                }

                @Override
                public void remap(PacketWrapper packetWrapper) throws Exception {
                    oldPacket.getRemapper().remap(packetWrapper);
                    super.remap(packetWrapper);
                }
            });
            map.put(packet, newPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recursiveMatchRank(JsonElement json) {
        if (json instanceof JsonArray) {
            for (JsonElement element : (JsonArray) json) {
                recursiveMatchRank(element);
            }
        }
        
        if (json instanceof JsonObject) {
            JsonObject object = (JsonObject) json;
            
            if (object.has("text") && object.has("color")) {
                String text = object.get("text").getAsString();
                Pattern pattern = Pattern.compile("\\[(.*?)]");
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    RanksManager.Rank rank = RanksManager.getRank(matcher.group(1));
                    if (rank != null) {
                        object.add("color", new JsonPrimitive(rank.getHexColor()));
                    }
                }
            }
            
            if (object.has("extra")) {
                recursiveMatchRank(object.get("extra"));
            }
        }
    }
}

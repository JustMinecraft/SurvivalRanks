package net.justminecraft.survivalranks;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewColors {

    public NewColors() {
        Protocol protocol = Via.getManager().getProtocolManager().getProtocol(Protocol1_16To1_15_2.class);

        try {
            Field outgoing = AbstractProtocol.class.getDeclaredField("clientbound");
            outgoing.setAccessible(true);
            Map<AbstractProtocol.Packet, AbstractProtocol.ProtocolPacket> map = (Map<AbstractProtocol.Packet, AbstractProtocol.ProtocolPacket>) outgoing.get(protocol);
            AbstractProtocol.Packet packet = new AbstractProtocol.Packet(State.PLAY, ClientboundPackets1_15.CHAT_MESSAGE.ordinal());
            AbstractProtocol.ProtocolPacket oldPacket = map.get(packet);
            AbstractProtocol.ProtocolPacket newPacket = new AbstractProtocol.ProtocolPacket(oldPacket.getState(), oldPacket.getOldID(), oldPacket.getNewID(), new PacketRemapper() {
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

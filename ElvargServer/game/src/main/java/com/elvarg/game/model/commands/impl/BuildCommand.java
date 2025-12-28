package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class BuildCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        if (parts.length < 2) {
            player.getPacketSender().sendMessage("Usage: ::build [object_id]");
            return;
        }

        try {
            int objectId = Integer.parseInt(parts[1]);

            // FIX 1: Use the simple constructor (ID, Location)
            // Most Elvarg versions set type=10 and rotation=0 by default with this one.
            // Arguments: ID, Location, Type(10), Face(0), PrivateArea(null)
            GameObject newObj = new GameObject(objectId, player.getLocation(), 10, 0, null);

            // FIX 2: Send the object directly to the player's client
            // Instead of "ObjectManager.spawnPersonal", we just tell the client "Draw this object"
            player.getPacketSender().sendObject(newObj);

            player.getPacketSender().sendMessage("You build object: " + objectId);

        } catch (NumberFormatException e) {
            player.getPacketSender().sendMessage("Please enter a valid number.");
        }
    }

    @Override
    public boolean canUse(Player player) {
        return player.getRights().equals(PlayerRights.OWNER) || player.getRights().equals(PlayerRights.DEVELOPER);
    }
}
package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location; // Or Position, depending on your server
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class HomeBuildCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        // 1. Calculate a unique height level for this player
        // (Index * 4) ensures no two players ever overlap
        int uniqueHeight = player.getIndex() * 4;

        // 2. Teleport them to the green empty zone
        // 2000, 4000 is usually empty. If not, try 2500, 2500
// A flat green area often used for testing
// Duel Arena (Center) - Guaranteed flat walkable area
// Duel Arena Center - Height 0 (Ground Level)
// Piscatoris Hunter Area (Massive Flat Green Zone)
// We use 'uniqueHeight' to make it private for each player
        Location houseLocation = new Location(1884, 5106, 0);
        player.moveTo(houseLocation);

        player.getPacketSender().sendMessage("Welcome to your private building zone!");

        // Note: We will add the "Load Furniture" code here later once we edit Player.java
    }

    @Override
    public boolean canUse(Player player) {
        // Allow everyone to use it, or restrict to Donators/Owners
        return true;
    }
}
package net.marketplace.models;

import java.util.UUID;

public class PendingTransaction {

    private UUID playerUUID;
    private double amount;

    public PendingTransaction(UUID playerUUID, double amount) {
        this.playerUUID = playerUUID;
        this.amount = amount;
    }

    //Getters
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public double getAmount(){
        return amount;
    }
}

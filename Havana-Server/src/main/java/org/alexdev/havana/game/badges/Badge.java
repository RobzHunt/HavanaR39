package org.alexdev.havana.game.badges;

public class Badge {
    private final String badgeCode; // nunca muda após criação
    private boolean equipped;
    private int slotId;

    public Badge(String badgeCode, boolean equipped, int slotId) {
        this.badgeCode = badgeCode;
        this.equipped = equipped;
        this.slotId = slotId;
    }

    public String getBadgeCode() { return badgeCode; }
    public boolean isEquipped()  { return equipped;  }
    public int getSlotId()       { return slotId;    }

    public void setEquipped(boolean equipped) { this.equipped = equipped; }
    public void setSlotId(int slotId)         { this.slotId = slotId;    }

    @Override
    public String toString() {
        return "Badge{code='" + badgeCode + "', equipped=" + equipped + ", slot=" + slotId + "}";
    }
}

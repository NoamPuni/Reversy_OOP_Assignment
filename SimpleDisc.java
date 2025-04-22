/**
 * a disc from a regular type place it in valid position and
 * flip all the discs that found between him and another disc
 * of te player in each direction
 */
public class SimpleDisc implements Disc {
    private Player owner;


    public SimpleDisc(Player owner) {
        this.owner = owner;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public void setOwner(Player player) {
        this.owner = player;
    }


    @Override
    public String getType() {
        return "⬤";
    }


}

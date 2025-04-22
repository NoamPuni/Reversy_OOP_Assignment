/**
 * Class of disc from bomb type when activate he flip all
 * the discs that next to him in all 8 directions
 */
public class BombDisc extends SimpleDisc {

    Player owner;

    public BombDisc(Player owner) {
        super(owner);
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    @Override
    public void setOwner(Player player) {
        this.owner = player;
    }

    @Override
    public String getType() {
        return "💣";
    }
}

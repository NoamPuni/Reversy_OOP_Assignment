/**
 * Class the presents a disc that is unflappable, ones he is placed
 * he can't be flipped, acts like a simple disc beside that
 */
public class UnflippableDisc extends SimpleDisc {
    Player owner;

    public UnflippableDisc(Player owner) {
        super(owner);
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    @Override
    public void setOwner(Player player) {
        this.owner = this.owner;
    }

    @Override
    public String getType() {
        return "⭕";
    }
}

/**
 * help class that represent a move on the board for each player
 * with the disc he placed in the position he clicked
 */
public class Move {
    public Position position;
    private Disc disc;

    private Player player;

    public Move(Position position, Disc disc, Player player) {
        this.position = position;
        this.disc = disc;
        this.player = player;
    }

    public Position getPosition() {
        return position;
    }

    public Disc getDisc() {
        return disc;
    }

    public Player getPlayer() {
        return player;
    }

    public Position position() {
        return this.position;
    }

    public Disc disc() {
        return this.disc;
    }

}

/**
 * Class that represents a human player that mean follow the
 * commands from a user and do it if possible
 */
public class HumanPlayer extends Player {


    public HumanPlayer(boolean isPlayerOne) {
        super(isPlayerOne);

    }

    public boolean isHuman() {
        return true;
    }

    @Override
    public String toString() {
        if (isPlayerOne) {
            return "Player 1 ";
        } else {
            return "Player 2 ";
        }
    }

}

/**
 * Class that represents an AI player that always choose the move
 * that flips the most discs possible
 */
public class GreedyAI extends AIPlayer {
    public GreedyAI(boolean isPlayerOne) {
        super(isPlayerOne);
    }

    @Override
    public Move makeMove(PlayableLogic gameStatus) {
        int mostFlipIndex = 0;
        for (int i = 0; i < gameStatus.ValidMoves().size(); i++) {
            //validMoves is built in a way that is going on the board from up to down and left to right
            if (mostFlipIndex <= gameStatus.countFlips(gameStatus.ValidMoves().get(i))) {
                mostFlipIndex = i;
            } //if there are some positions with the same potential flips number we take the most down and most right one
        }

        return new Move(gameStatus.ValidMoves().get(mostFlipIndex), new SimpleDisc(this), this);
    }


    public String toString() {
        if (isPlayerOne) {
            return "Player 1 ";
        } else {
            return "Player 2";
        }
    }
}



/**
 * a class that represents an un human player that play
 * his move in a random way
 */

public class RandomAI extends AIPlayer{
    public RandomAI(boolean isPlayerOne) {
        super(isPlayerOne);

    }
    public String toString() {
        if (isPlayerOne){
            return "Player 1 ";
        }
        else {
            return "Player 2";
        }
    }
    @Override
    public Move makeMove(PlayableLogic gameStatus) {

        //Random position from the valid ones
        int options = gameStatus.ValidMoves().size();
        int randPosition = randomNumber(options);

        //Random disc
        int numD = randomNumber(3);

        return new Move(gameStatus.ValidMoves().get(randPosition),randomDisc(numD), this);

    }
    public boolean isHuman() {
        return false;
    }

    private int randomNumber (int maximum){//get a random disc type
        double rnd = Math.random();
        rnd = (rnd * maximum);
        int randNum = (int) rnd;
        return randNum;
    }
    private Disc randomDisc (int num){
        if (num == 0){
            if (this.getNumber_of_bombs() !=0 ){
                Disc randomDisc = new BombDisc(this);
                return randomDisc;
            }
        }
        else if (num == 1){
            if (this.getNumber_of_unflippedable()!=0){
                Disc randomDisc = new UnflippableDisc(this);
                return randomDisc;
            }
        }

        Disc randomDisc = new SimpleDisc(this);
        return randomDisc;


    }
}

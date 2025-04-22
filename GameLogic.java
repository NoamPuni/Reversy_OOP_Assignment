import java.sql.Array;
import java.util.*;

/**
 * The main class in the project, manages all the moves, is responsible for updating
 * the board, makes sure everything happens according to the rules, calculates valid
 * moves and advances the game, changes turns, allows to undo a move, confirms that
 * the game ends and announces a winner
 */
public class GameLogic implements PlayableLogic {
    private Disc[][] board = new Disc[getBoardSize()][getBoardSize()];
    private Stack<Disc[][]> currentBoards = new Stack<>();
    private Stack<Move> moves = new Stack<>();
    private ArrayList<ArrayList<Position>> sequences = new ArrayList<>(); // keeping all the possible sequences(list of positions) of possible moves in this list
    private Player firstPlayer = new HumanPlayer(isFirstPlayerTurn());
    private Player secondPlayer = new HumanPlayer(!isFirstPlayerTurn());

    public GameLogic() {
        reset();
    }

    /**
     * Checking for the given position if it's possible to put there a disc
     * if it's a possible position update the board with the given disc
     * and return true.
     * @param a The position for locating a new disc on the board.
     * @param disc the disc that the player tried to place in this position
     * @return if it's a possible position return true
     */
    @Override
    public boolean locate_disc(Position a, Disc disc) {
        ArrayList<Position> valid = new ArrayList<>(); // finds the player's valid moves to know if the place the disc was placed is a valid move
        for (int i = 0; i < sequences.size(); i++) {
            for (int j = 0; j < sequences.get(i).size(); j++) {
                valid.add(sequences.get(i).getLast());
            }
        }
        int row = a.getRow();
        int col = a.getCol();
        if (!isInBounds(row, col)) { // check if the position is in board bounds
            return false;
        }
        if (getDiscAtPosition(a) == null) {

            if (isFirstPlayerTurn() && valid.contains(a)) { // if first player disc
                Disc[][] tempBoard = new Disc[getBoardSize()][getBoardSize()];
                for (int i = 0; i < tempBoard.length; i++) {
                    System.arraycopy(board[i], 0, tempBoard[i], 0, tempBoard[i].length);
                }
                deepBoardCopy(this.board, tempBoard);
                currentBoards.push(tempBoard); //copy the board and save in a stack for undo method
                if (disc.getType().equals("⭕")) {
                    if (this.firstPlayer.number_of_unflippedable > 0) {
                        this.firstPlayer.number_of_unflippedable--;
                    } else return false;
                }
                if (disc.getType().equals("💣")) {
                    if (this.firstPlayer.number_of_bombs > 0) {
                        this.firstPlayer.number_of_bombs--;
                    } else {
                        return false;
                    }
                }
                board[row][col] = disc;
                Move move = new Move(a, disc, getFirstPlayer());
                System.out.println();
                System.out.println(getFirstPlayer() + " placed the " + disc.getType() + " in (" + a.getRow() + "," + a.getCol() + ")");
                flipping(a);
                moves.push(move); // for tracking which of the players turn

            } else if (!isFirstPlayerTurn() && valid.contains(a)) { // if second player disc
                Disc[][] tempBoard = new Disc[getBoardSize()][getBoardSize()];
                for (int i = 0; i < tempBoard.length; i++) {
                    System.arraycopy(board[i], 0, tempBoard[i], 0, tempBoard[i].length);
                }
                deepBoardCopy(this.board, tempBoard);
                currentBoards.push(tempBoard);
                if (disc.getType().equals("⭕")) {
                    if (this.secondPlayer.number_of_unflippedable > 0) {
                        this.secondPlayer.number_of_unflippedable--;
                    } else return false;
                }
                if (disc.getType().equals("💣")) {
                    if (this.secondPlayer.number_of_bombs > 0) {
                        this.secondPlayer.number_of_bombs--;
                    } else {
                        return false;
                    }
                }
                board[row][col] = disc;
                Move move = new Move(a, disc, getSecondPlayer());
                System.out.println();
                System.out.println(getSecondPlayer() + "placed the " + disc.getType() + " in (" + a.getRow() + "," + a.getCol() + ")");
                flipping(a);
                moves.push(move); // for tracking which of the players turn

            }
            for (int i = 0; i < sequences.size(); i++) {
                sequences.remove(i);
            }
            return true;

        }
        return false;
    }

    /**
     * locate the disc on the board for the given position
     * @param position The position for which to retrieve the disc.
     * @return the disc in this position on the board(null if empty)
     */
    @Override
    public Disc getDiscAtPosition(Position position) {

        return board[position.getRow()][position.getCol()];
    }

    /**
     * by default the board size is 8X8
     * @return 8
     */
    @Override
    public int getBoardSize() {
        return 8;
    }

    /**
     * checking for all the valid moves on the current situation in the game for the player that plays now
     * @return the valid moves
     */
    @Override
    public List<Position> ValidMoves() {
        ArrayList<Position> validMoves = new ArrayList<>();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] != null) {
                    if (isFirstPlayerTurn() && board[i][j].getOwner().equals(getFirstPlayer())) {
                        validMoves.addAll(directions(new Position(i, j)));

                    }
                    if (!isFirstPlayerTurn() && board[i][j].getOwner().equals(getSecondPlayer())) {
                        validMoves.addAll(directions(new Position(i, j)));
                    }
                }
            }
        }
        return validMoves;
    }

    /**
     * counting the possible flips for each valid move
     * @param a the position that the method check the number of flips if disc will be placed there
     * @return the number of possible flips for disc that will be placed in this position
     */
    @Override
    public int countFlips(Position a) {
        int flips = 0;
        int duplicateCounter = 0;
        HashSet<Position> visited = new HashSet<>();
        Set<Position> flipsFromBomb = new HashSet<>();

        // add the last move position to visited, if available
        if (!moves.isEmpty()) {
            visited.add(moves.peek().position);
        }

        for (int i = 0; i < sequences.size(); i++) {
            List<Position> sequence = sequences.get(i);
            if (sequence.get(sequence.size() - 1).equals(a)) { // if the sequence ends at position `a`
                int counterOfUF = 0; // counter for unflappable discs
                int counterOfBomb = 0; // counter for bomb discs

                for (int j = 1; j < sequence.size(); j++) { // skip the first position
                    Position current = sequence.get(j);
                    Disc disc = getDiscAtPosition(current);

                    if (disc != null) {
                        String type = disc.getType();

                        if ("⭕".equals(type)) {
                            counterOfUF++; // count unflappable discs
                        } else if ("💣".equals(type) && !visited.contains(current)) {
                            flipsFromBomb = new HashSet<>((checkAroundBomb(current, new ArrayList<>(visited))));
                            counterOfBomb += flipsFromBomb.size(); // recursive bomb check

                            visited.add(current);
                        }
                    }
                }
                if (sequence.size() > 0) {
                    for (int j = 0; j < sequence.size(); j++) {
                        if (flipsFromBomb.contains(sequence.get(j))) {
                            duplicateCounter++;
                        }
                    }
                }
                // calculate flips for this sequence
                flips += sequence.size() - 2 - counterOfUF + counterOfBomb - duplicateCounter;
            }
        }

        return flips;
    }

    @Override
    public Player getFirstPlayer() {
        return firstPlayer;
    }

    @Override
    public Player getSecondPlayer() {
        return secondPlayer;
    }

    @Override
    public void setPlayers(Player player1, Player player2) {
        this.firstPlayer = player1;
        this.secondPlayer = player2;
    }

    @Override
    public boolean isFirstPlayerTurn() {

        return moves.size() % 2 == 0;

    }

    /**
     * checking if game finish if there is no valid moves and check who is the winner by counting
     * the discs on the board for each player
     * @return true if the game finished
     */
    @Override
    public boolean isGameFinished() {
        int discsOfFirst = 0;
        int discsOfSecond = 0;

        if (ValidMoves().isEmpty()) {
            for (int i = 0; i < getBoardSize(); i++) {
                for (int j = 0; j < getBoardSize(); j++) {
                    Position currentPosition = new Position(i, j);
                    Disc disc = getDiscAtPosition(currentPosition);
                    if (disc != null) {
                        if (disc.getOwner() == getFirstPlayer()) {
                            discsOfFirst++;
                        } else if (disc.getOwner() == getSecondPlayer()) {
                            discsOfSecond++;
                        }
                    }
                }
            }

            // declaring the winner
            if (discsOfFirst > discsOfSecond) {
                this.firstPlayer.wins++;
                System.out.println("Player 1 wins with " + discsOfFirst + " discs! Player 2 had " + discsOfSecond + " discs.");
            } else if (discsOfFirst < discsOfSecond) {
                this.secondPlayer.wins++;
                System.out.println("Player 2 wins with " + discsOfSecond + " discs! Player 1 had " + discsOfFirst + " discs.");
            } else {
                System.out.println("It's a tie! Both players have " + discsOfFirst  + " discs.");
            }
            return true;
        }
        return false; //game isn't over yet
    }

    /**
     * initialize the game for the starting positions, the constructor also use this method
     */
    @Override
    public void reset() {
        int firstWins = 0;
        int secondWins = 0;
        board = new Disc[getBoardSize()][getBoardSize()];
        moves.clear();
        sequences = new ArrayList<>();
        if (this.firstPlayer != null) {
            if (this.firstPlayer.wins > 0) {
                firstWins = this.firstPlayer.wins;
            }
        }
        if (this.secondPlayer != null) {
            if (this.secondPlayer.wins > 0) {
                secondWins = this.secondPlayer.wins;
            }
        }
        this.firstPlayer.number_of_bombs = 3;
        this.secondPlayer.number_of_bombs = 3;
        this.firstPlayer.number_of_unflippedable = 2;
        this.secondPlayer.number_of_unflippedable= 2;
        this.firstPlayer.wins = firstWins;
        this.secondPlayer.wins = secondWins;
        board[3][3] = new SimpleDisc(getFirstPlayer());
        board[4][4] = new SimpleDisc(getFirstPlayer());
        board[3][4] = new SimpleDisc(getSecondPlayer());
        board[4][3] = new SimpleDisc(getSecondPlayer());

    }

    /**
     * undo the last move on the board and update all needed
     */
    @Override
    public void undoLastMove() {

        if (!currentBoards.isEmpty() && !moves.isEmpty()) {
            System.out.println("Undoing last move:");
            // restore the board to the previous state
            deepBoardCopy(currentBoards.pop(), this.board);
            // get data about the last move
            Move lastMove = moves.peek();
            Position lastPosition = lastMove.getPosition();
            Disc lastDisc = lastMove.getDisc();

            System.out.println("\tUndo: removing " + lastDisc.getType() + " from (" + lastPosition.getRow() + ", " + lastPosition.getCol() + ")");

            // restore special disc counts if necessary
            if ("⭕".equals(lastDisc.getType())) {
                lastMove.getPlayer().number_of_unflippedable++;
            } else if ("💣".equals(lastDisc.getType())) {
                lastMove.getPlayer().number_of_bombs++;
            }
            for (int i = 0; i < getBoardSize(); i++) {
                for (int j = 0; j < getBoardSize(); j++) {
                    Position currentPosition = new Position(i, j);
                    Disc currentDisc = getDiscAtPosition(currentPosition);

                    // if the disc belongs to the current player but was flipped, revert it
                    if (currentDisc != null && currentDisc.getOwner() == lastMove.getPlayer()) {
                        // change owner back to the opponent
                        if (isFirstPlayerTurn()) {
                            currentDisc.setOwner(getSecondPlayer());
                        } else {
                            currentDisc.setOwner(getFirstPlayer());
                        }
                        System.out.println("\tUndo: flipping back " + currentDisc.getType() + " in (" + i + ", " + j + ")");
                    }
                }
            }
        }
        if (!moves.isEmpty()) {
            moves.pop();

            ValidMoves(); // recalculate valid moves

        } else {
            System.out.println("No previous move available to undo");
        }
    }

    /**
     * make a copy of the board with all the information on the discs that are on the board
     * @param originalBoard the original board
     * @param copiedBoard the copy
     */
    private void deepBoardCopy(Disc[][] originalBoard, Disc[][] copiedBoard) {
        for (int i = 0; i < getBoardSize(); i++) {
            for (int j = 0; j < getBoardSize(); j++) {
                if (originalBoard[i][j] != null) { // create a new Disc object for the copied board

                    if (originalBoard[i][j].getType().equals("⬤")) {
                        copiedBoard[i][j] = new SimpleDisc(originalBoard[i][j].getOwner());
                    }
                    if (originalBoard[i][j].getType().equals("⭕")) {
                        copiedBoard[i][j] = new UnflippableDisc(originalBoard[i][j].getOwner());
                    }
                    if (originalBoard[i][j].getType().equals("💣")) {
                        copiedBoard[i][j] = new BombDisc(originalBoard[i][j].getOwner());
                    }

                } else {
                    copiedBoard[i][j] = null; // ensure null is copied correctly
                }

            }
        }
    }

    /**
     * a helping method for valid moves for each possible directions give the possible paths in the board
     * the last in each list is a position that go to Valid moves
     * @param p scan for this position all possible moves on the board
     * @return All the positions that are in the last place of each list that represents a possible route on the board are the Valid moves
     */
    private ArrayList<Position> directions(Position p) {
        boolean turnOfFirstPlayer = isFirstPlayerTurn();// checking who is attacker and who is a defender now
        Player currentPlayer;
        Player opponentPlayer;
        if (turnOfFirstPlayer) {
            currentPlayer = getFirstPlayer();
            opponentPlayer = getSecondPlayer();
        } else {
            currentPlayer = getSecondPlayer();
            opponentPlayer = getFirstPlayer();
        }
        ArrayList<Position> positions = new ArrayList<>();

        int[][] directions = {
                {0, -1}, {0, 1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int i = 0; i < directions.length; i++) {// for each one of the 8 directions surrounding the disc
            ArrayList<Position> temp = helpDirections(p, directions[i], currentPlayer, opponentPlayer);
            if (temp != null) {
                temp.add(0, p);
                sequences.add(temp); // add all the valid paths for every disc of the current player
                filterSequences(currentPlayer);



                if (countFlips(temp.getLast()) > 0) { // to remove a move that flips 0 disk because of an unflappable disc
                    temp.getLast().potentialFlips = 0; // for not count the flips twice
                    positions.add(temp.getLast()); // add the last in the list he is the valid move
                }
            }
        }

        return positions;
    }

    /**
     * a method for locateDisc after the move is valid checking how it influences the board
     * and if their discs that need to be flipped
     * @param clicked the position where the disc was placed at thos turn
     */
    private void flipping(Position clicked) {
        Player currentPlayer;
        if (isFirstPlayerTurn()) {
            currentPlayer = getFirstPlayer();
        } else {
            currentPlayer = getSecondPlayer();
        }
        for (List<Position> sequence : sequences) {
            // check if the sequence ends at the clicked position and starts with the current player's disc
            if (sequence.getLast().equals(clicked) && getDiscAtPosition(sequence.getFirst()).getOwner() == currentPlayer) {
                // flip discs in the sequence, excluding the first and last
                for (int i = 1; i < sequence.size() - 1; i++) {
                    Position currentPos = sequence.get(i);
                    Disc disc = getDiscAtPosition(currentPos);

                    // update the owner of the disc
                    disc.setOwner(currentPlayer);

                    // handle bomb flipping
                    if ("💣".equals(disc.getType())) {
                        bombFlip(currentPlayer, currentPos);
                    }

                    // log the flip, excluding unflappable discs
                    if (!"⭕".equals(disc.getType())) {
                        System.out.println(currentPlayer + " flipped the " + disc.getType() + " in (" + currentPos.getRow() + "," + currentPos.getCol() + ")");
                    }
                }
            }
        }
    }

    /**
     * in flipping if the placed disc flipped a bomb disc checking for the influence
     * on the board from flipping this bomb disc
     * @param current the player whose turn it is
     * @param pos the current position that was flipped in flipping method
     */
    private void bombFlip(Player current, Position pos) {
        int[][] directions = {
                {0, -1}, {0, 1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int i = 0; i < directions.length; i++) {
            int newRow = pos.getRow() + directions[i][0];
            int newCol = pos.getCol() + directions[i][1];
            Position newPos = new Position(newRow, newCol);
            if (isInBounds(newRow,newCol)) {///////////////////////////////////////////
                Disc targetDisc = getDiscAtPosition(newPos);
                if (targetDisc != null && targetDisc.getOwner() != current) {
                    // flip the disc
                    targetDisc.setOwner(current);
                    System.out.println("Player " + current + " flipped the " + targetDisc.getType() + " in (" + newRow + ", " + newCol + ")");
                    // recursively handle bomb flipping
                    if ("💣".equals(targetDisc.getType())) {
                        bombFlip(current, newPos);
                    }
                }
            }//////////////////////////////////
        }
    }


    /**
     * a helping method to directions, checking for all possible directions that are
     * potential to be a valid paths and after that map only the valid ones to return them to directions
     * @param pos the given position to be checked
     * @param direction the one of eight possible direction for the disc
     * @param current the player whose turn it is
     * @param opponent The player who didn't take his turn
     * @return all valid paths
     */
    private ArrayList<Position> helpDirections(Position pos, int[] direction, Player current, Player opponent) {

        ArrayList<Position> checkValid = new ArrayList<>(); // list of paths to a potenial valid move

        int newRow = pos.getRow() + direction[0];
        int newCol = pos.getCol() + direction[1];
        Position currentPos = new Position(newRow, newCol);
        while (isInBounds(newRow, newCol) && getDiscAtPosition(currentPos) != null) { //check all the paths that can be a valid moves
            currentPos = new Position(newRow, newCol);
            checkValid.add(currentPos);
            newRow = currentPos.getRow() + direction[0];
            newCol = currentPos.col() + direction[1];
        }

        if (!checkValid.isEmpty()) {
            if (getDiscAtPosition(checkValid.getFirst()).getOwner() != current) {
                if (getDiscAtPosition(checkValid.getFirst()) != null) {
                    for (int i = 0; i < checkValid.size(); i++) {
                        if (getDiscAtPosition(checkValid.get(i)) != null) {
                            if (getDiscAtPosition(checkValid.get(i)).getOwner() == current) {
                                break;
                            }
                        } else if (i == 0) {

                            break;
                        } else { //this position's disc is null
                            for (int j = i + 1; j < checkValid.size(); j++) {
                                checkValid.remove(j);
                            }
                        }
                        if (checkValid.get(i) == checkValid.getLast()) {
                            if (isEdgeCase(checkValid.getLast())) {
                                if (getDiscAtPosition(checkValid.getLast()) != null) { // need to check for the upper bound also
                                    return null;
                                }
                            }

                            return checkValid;
                        }
                    }

                }
            }
        }

        return null;
    }

    /**
     * checking for edges cases for the given paths in helpDirection, if checking
     * a position that stands on one of the board bounds check it withe special conditions
     * @param position the position who is on one on the board bounds
     * @return what the possible direction for this positions
     */
    private boolean isEdgeCase(Position position) {
        int[][] directions = {
                {0, -1}, {0, 1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int i = 0; i < directions.length; i++) {
            int newRow = position.getRow() + directions[i][0];
            int newCol = position.getCol() + directions[i][1];
            Position newPos = new Position(newRow, newCol);
            if (newPos.getRow() == getBoardSize() - 1 || newPos.getCol() == getBoardSize() - 1 || newPos.getRow() < 0 || newPos.getCol() < 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * checking if the position is in bounds
     * @param row the roe of given position
     * @param col the column of given position
     * @return true if it is in bounds
     */
    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
    }

    /**
     * remove all duplicate sequences for avoid errors and double check
     * @param current player whose turn it is
     */
    private void filterSequences(Player current) {
        for (int i = 0; i < sequences.size(); i++) {
            // check if the first element of the sequence is null
            if (sequences.get(i).getFirst() == null) {
                sequences.remove(i);
                i--; // adjust the index after removal
                continue;
            }

            // check if the owner of the first disc in the sequence is not the current player
            if (getDiscAtPosition(sequences.get(i).getFirst()).getOwner() != current) {
                sequences.remove(i);
                i--; // adjust the index after removal
                continue;
            }

            // check for duplicate sequences
            for (int k = i + 1; k < sequences.size(); k++) {
                // ensure both sequences have elements
                if (sequences.get(k).size() > 0 && sequences.get(i).size() > 0) {
                    if (sequences.get(i).getLast().equals(sequences.get(k).getLast()) &&
                            sequences.get(i).getFirst().equals(sequences.get(k).getFirst())) {
                        sequences.remove(k);
                        k--; // adjust the index after removal
                    }
                }
            }
        }
    }

    /**
     * if locate a bomb when scan the board checking if there is another bomb on her eight
     * close directions for calculate the potential count flips, for avoiding duplicates
     * @param position the given position
     * @param visited mark all the positions that already counted
     * @return a set of positions that need to be in count flips without duplicates
     */
    private Set<Position> checkAroundBomb(Position position, ArrayList<Position> visited) { // "position": a position of bomb disc
        int counter = 0;
        if (visited.contains(position)) {
            return null;
        }
        visited.add(position);
        Player currentPlayer;
        Player opponentPlayer;
        if (isFirstPlayerTurn()) {
            currentPlayer = getFirstPlayer();
            opponentPlayer = getSecondPlayer();
        } else {
            currentPlayer = getSecondPlayer();
            opponentPlayer = getFirstPlayer();
        }
        int[][] directions = {{0, -1}, {0, 1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int i = 0; i < directions.length; i++) {
            int newRow = position.getRow() + directions[i][0];
            int newCol = position.getCol() + directions[i][1];
            Position newPos = new Position(newRow, newCol);

            if (isInBounds(newRow,newCol)) {//////////////////////////////
                Disc discAtNewPos = getDiscAtPosition(newPos);
                if (discAtNewPos != null && discAtNewPos.getOwner() == opponentPlayer && !visited.contains(newPos)) {
                    visited.add(newPos);
                    counter++;
                    String type = discAtNewPos.getType();
                    if ("⭕".equals(type)) {
                        visited.remove(visited.size() - 1);
                        counter--;
                    } else if ("💣".equals(type)) {
                        checkAroundBomb(newPos, visited);
                    }
                }
            }///////////////////////////////////////////
        }
        return new HashSet<>(visited);
    }
}

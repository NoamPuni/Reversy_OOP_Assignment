import java.util.ArrayList;
import java.util.Objects;

/**
 * helper class to indicates a coordinates on the board and get information
 * if there is a disc in there and if it is taking his data
 */
public class Position {
    private int row;
    private int col;
    public int potentialFlips;
    public ArrayList<ArrayList<Position>> sequences = new ArrayList<>();


    public Position(int row, int col) {
        this.row = row;
        this.col = col;
        this.potentialFlips = 0;

    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }


    public int row() {
        return row;
    }

    public int col() {
        return col;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true; // check if the objects are the same instance
        if (obj == null || getClass() != obj.getClass()) return false; // Ensure type compatibility

        Position other = (Position) obj; // cast to Position
        return this.row == other.row && this.col == other.col; // Compare fields
    }

    @Override
    public String toString() {
        return "(" + this.getRow() + "," + this.getCol() + ")";
    }
}
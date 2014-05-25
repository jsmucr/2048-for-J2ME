package game2048;

/**
 * Datová třída sloužící k předávání informací o pohybu na desce.
 * 
 * @author Jan Šmucr
 */
public final class Movement
{
    public final int sourceRow;
    public final int sourceCol;
    public final int fieldsDistance;
    public final int direction;
    public final int destinationRow;
    public final int destinationCol;
    
    public Movement(final int sourceRow, final int sourceCol, final int fieldsDistance, final int direction)
    {
        this.sourceRow = sourceRow;
        this.sourceCol = sourceCol;
        this.fieldsDistance = fieldsDistance;
        this.direction = direction;
        
        switch (direction)
        {
            case Direction.LEFT:
                destinationRow = sourceRow;
                destinationCol = sourceCol - fieldsDistance;
                break;
            case Direction.UP:
                destinationRow = sourceRow - fieldsDistance;
                destinationCol = sourceCol;
                break;
            case Direction.RIGHT:
                destinationRow = sourceRow;
                destinationCol = sourceCol + fieldsDistance;
                break;
            case Direction.DOWN:
                destinationRow = sourceRow + fieldsDistance;
                destinationCol = sourceCol;
                break;
            default:
                destinationRow = sourceRow;
                destinationCol = sourceCol;
                break;
        }
    }
}

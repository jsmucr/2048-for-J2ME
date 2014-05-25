package game2048;

/**
 * Konstanty směrů tahu.
 * 
 * @author Jan Šmucr.
 */
public final class Direction
{
    public static final int LEFT = 1;
    public static final int UP = 2;
    public static final int RIGHT = 4;
    public static final int DOWN = 8;  
    public static final int HORIZONTAL = LEFT | RIGHT;
    public static final int VERTICAL = UP | DOWN;
}

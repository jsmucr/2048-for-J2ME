package game2048;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

/**
 * <p>Třída reprezentující datovou strukturu herní desky. Událostem na desce lze
 * naslouchat prostřednictvím instance {@linkplain BoardChangeListener}.</p>
 * <p>Desku lze pro začátek hry inicializovat metodou {@link #init() init} nebo
 * předat její metodě {@link #loadState(java.io.DataInputStream) loadState}
 * stream dat, do kterého předtím sama uložila svůj stav prostřednictvím metody
 * {@link #saveState(java.io.DataOutputStream) saveState}.
 * 
 * @author Jan Šmucr
 */
public final class Board
{

    public static final int BOARD_WIDTH = 4;
    public static final int BOARD_HEIGHT = 4;

    private static final int FIELDS_COUNT = BOARD_WIDTH * BOARD_HEIGHT;
    private static final int BASE = 2;
    private static final Random random = new Random(System.currentTimeMillis());

    private final int[][] board;
    private final Vector listeners = new Vector(2);
    private int fieldsOccupied = 0;
    private boolean boardClean = true;

    public Board()
    {
        board = new int[BOARD_HEIGHT][];
        for (int i = 0; i < BOARD_HEIGHT; i++)
        {
            board[i] = new int[BOARD_WIDTH];
        }
    }

    /**
     * Inicializuje desku do stavu, jaký odpovídá začátku hry.
     */
    public final void init()
    {
        if (boardClean)
        {
            boardClean = false;
        } else
        {
            fieldsOccupied = 0;
            for (int row = 0; row < BOARD_HEIGHT; row++)
            {
                for (int col = 0; col < BOARD_WIDTH; col++)
                {
                    removeNumber(row, col);
                }
            }
        }

        createNumber();
        createNumber();
    }

    /**
     * Vytvoří na desce na libovolném volném místě číslo 2 nebo 4.
     * @return <code>true</code> pokud došlo k vytvoření čísla. <code>false
     * </code> znamená, že je deska plná.
     */
    public final boolean createNumber()
    {
        if (fieldsOccupied == FIELDS_COUNT)
        {
            return false;
        }

        int fieldIndex = random.nextInt(FIELDS_COUNT - fieldsOccupied);
        for (int row = 0; row < BOARD_HEIGHT; row++)
        {
            for (int col = 0; col < BOARD_WIDTH; col++)
            {
                if (board[row][col] > 0)
                {
                    continue;
                }
                if (fieldIndex == 0)
                {
                    final boolean doubleVal = random.nextInt(2) == 1;
                    createNumber(row, col, doubleVal ? BASE : BASE * 2);
                    return true;
                }
                fieldIndex--;
            }
        }

        return false;
    }

    private void createNumber(final int row, final int col, final int number)
    {
        removeNumber(row, col);
        board[row][col] = number;
        notifyNumberCreated(row, col);
    }

    /**
     * Provede tah na desce ve zvoleném směru.
     * @param direction Směr tahu (jedna z konstant ve třídě {@linkplain
     * Direction}.
     * @return <code>true</code> pokud k tahu došlo, <code>false</code> pokud
     * není tah v daném směru možný.
     */
    public final boolean move(final int direction)
    {
        switch (direction)
        {
            case Direction.LEFT:
                return move(direction, 0, BOARD_WIDTH - 1, BOARD_HEIGHT);
            case Direction.UP:
                return move(direction, 0, BOARD_HEIGHT - 1, BOARD_WIDTH);
            case Direction.RIGHT:
                return move(direction, BOARD_WIDTH - 1, 0, BOARD_HEIGHT);
            case Direction.DOWN:
                return move(direction, BOARD_HEIGHT - 1, 0, BOARD_WIDTH);
            default:
                throw new IllegalArgumentException("direction");
        }
    }

    private boolean move(final int direction, final int startB, final int endB, final int endA)
    {
        boolean moved = false;

        final int dB = endB > startB ? 1 : -1;
        final boolean aIsRow = (direction & Direction.HORIZONTAL) != 0;

        for (int a = 0; a != endA; a++)
        {
            int currentB = startB;
            int limit = startB - dB;
            while (currentB != endB)
            {
                currentB += dB;
                int number = aIsRow ? board[a][currentB] : board[currentB][a];
                if (number == 0)
                {
                    continue;
                }

                int targetB = currentB;
                boolean join = false;
                for (int b = targetB - dB; b != limit; b -= dB)
                {
                    final int targetNumber = aIsRow ? board[a][b] : board[b][a];
                    if (targetNumber == 0)
                    {
                        targetB = b;
                        continue;
                    }
                    if (targetNumber == number)
                    {
                        targetB = b;
                        join = true;
                    }
                    break;
                }

                if (targetB == currentB)
                {
                    continue;
                }

                if (join)
                {
                    if (aIsRow)
                    {
                        board[a][targetB] = 2 * number;
                        notifyNumbersJoined(a, currentB, (currentB - targetB) * dB, direction);
                    } else
                    {
                        board[targetB][a] = 2 * number;
                        notifyNumbersJoined(currentB, a, (currentB - targetB) * dB, direction);
                    }
                    limit = targetB;
                } else
                {
                    if (aIsRow)
                    {
                        board[a][targetB] = number;
                        notifyNumberMoved(a, currentB, (currentB - targetB) * dB, direction);
                    } else
                    {
                        board[targetB][a] = number;
                        notifyNumberMoved(currentB, a, (currentB - targetB) * dB, direction);
                    }
                }

                if (aIsRow)
                {
                    board[a][currentB] = 0;
                } else
                {
                    board[currentB][a] = 0;
                }

                moved = true;
            }
        }

        return moved;
    }

    private void notifyNumberCreated(final int row, final int col)
    {
        fieldsOccupied++;
        for (int i = 0; i < listeners.size(); i++)
        {
            final Object element = listeners.elementAt(i);
            if (element instanceof BoardChangeListener)
            {
                ((BoardChangeListener) element).onNumberCreated(this, row, col, fieldsOccupied == FIELDS_COUNT);
            }
        }
    }

    private void notifyNumbersJoined(final int sourceRow, final int sourceCol, final int fieldsDistance, final int direction)
    {
        fieldsOccupied--;
        final Movement movement = new Movement(sourceRow, sourceCol, fieldsDistance, direction);
        for (int i = 0; i < listeners.size(); i++)
        {
            final Object element = listeners.elementAt(i);
            if (element instanceof BoardChangeListener)
            {
                ((BoardChangeListener) element).onNumbersJoined(this, movement);
            }
        }
    }

    private void notifyNumberMoved(final int sourceRow, final int sourceCol, final int fieldsDistance, final int direction)
    {
        final Movement movement = new Movement(sourceRow, sourceCol, fieldsDistance, direction);
        for (int i = 0; i < listeners.size(); i++)
        {
            final Object element = listeners.elementAt(i);
            if (element instanceof BoardChangeListener)
            {
                ((BoardChangeListener) element).onNumberMoved(this, movement);
            }
        }
    }

    private void notifyNumberRemoved(final int row, final int col)
    {
        for (int i = 0; i < listeners.size(); i++)
        {
            final Object element = listeners.elementAt(i);
            if (element instanceof BoardChangeListener)
            {
                ((BoardChangeListener) element).onNumberRemoved(this, row, col);
            }
        }
    }

    /**
     * Vrací číslo na desce na daných souřadnicích.
     * @param row Řádka.
     * @param col Sloupec.
     * @return Číslo na desce, resp. 0, pokud je pole prázdné.
     */
    public final int getNumber(final int row, final int col)
    {
        return board[row][col];
    }

    /**
     * Přidá posluchače událostí na desce.
     * @param listener Posluchač.
     */
    public final void addListener(final BoardChangeListener listener)
    {
        listeners.addElement(listener);
    }

    /**
     * Odebere posluchače událostí na desce.
     * @param listener Posluchač.
     */
    public final void removeListener(final BoardChangeListener listener)
    {
        listeners.removeElement(listener);
    }

    /**
     * @return Výška desky.
     */
    public final int getRows()
    {
        return BOARD_HEIGHT;
    }

    /**
     * @return Šířka desky.
     */
    public final int getCols()
    {
        return BOARD_WIDTH;
    }

    /**
     * Zjistí, zda ještě existuje nějaký tah, který lze provést.
     * @return <code>true</code> nebo <code>false</code>.
     */
    public final boolean canMove()
    {
        for (int row = 0; row < BOARD_HEIGHT; row++)
        {
            for (int col = 0; col < BOARD_WIDTH; col++)
            {
                final int number = board[row][col];
                if (row > 0)
                {
                    final int left = board[row - 1][col];
                    if ((left == 0) || (left == number))
                    {
                        return true;
                    }
                }
                if (row < BOARD_HEIGHT - 1)
                {
                    final int right = board[row + 1][col];
                    if ((right == 0) || (right == number))
                    {
                        return true;
                    }
                }
                if (col > 0)
                {
                    final int top = board[row][col - 1];
                    if ((top == 0) || (top == number))
                    {
                        return true;
                    }
                }
                if (col < BOARD_WIDTH - 1)
                {
                    final int bottom = board[row][col + 1];
                    if ((bottom == 0) || (bottom == number))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Načte stav desky tak, jak byl předtím uložen metodou {@link
     * #saveState(java.io.DataOutputStream) saveState}.
     * @param stream Proud nastavený na začátek dat reprezentujících stav desky.
     * @throws IOException Chyba při čtení proudu dat.
     */
    public final void loadState(final DataInputStream stream) throws IOException
    {
        for (int row = 0; row < BOARD_HEIGHT; row++)
        {
            for (int col = 0; col < BOARD_WIDTH; col++)
            {
                final int number = stream.readInt();
                if (!isPositivePowerOfTwo(number))
                {
                    continue;
                }

                boardClean = false;
                createNumber(row, col, number);
            }
        }
    }

    private boolean isPositivePowerOfTwo(final int number)
    {
        if (number < 2)
        {
            return false;
        }
        final char[] binary = Integer.toBinaryString(number).toCharArray();
        boolean setBitFound = false;
        for (int i = 0; i < binary.length; i++)
        {
            if (binary[i] == '0')
            {
                continue;
            }
            if (setBitFound)
            {
                return false;
            }
            setBitFound = true;
        }

        return true;
    }

    /**
     * Uloží stav desky do předaného proudu dat.
     * @param stream Proud.
     * @throws IOException Chyba při zápisu do proudu.
     */
    public final void saveState(final DataOutputStream stream) throws IOException
    {
        for (int row = 0; row < BOARD_HEIGHT; row++)
        {
            for (int col = 0; col < BOARD_WIDTH; col++)
            {
                stream.writeInt(board[row][col]);
            }
        }
    }

    private void removeNumber(final int row, final int col)
    {
        if (board[row][col] > 0)
        {
            notifyNumberRemoved(row, col);
        }
        board[row][col] = 0;
    }

    /**
     * @return <code>true</code> pokud je deska úplně prázdná.
     */
    public final boolean isEmpty()
    {
        return fieldsOccupied == 0;
    }
}

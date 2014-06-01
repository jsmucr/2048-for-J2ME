package game2048.scene;

import game2048.Board;
import game2048.BoardChangeListener;
import game2048.Direction;
import game2048.Movement;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

/**
 * <p>Vykresluje na displej herní scénu a zachytává uživatelský vstup. Ten lze
 * sledovat prostřednictvím implementace třídy
 * {@linkplain UserActivityListener}.</p>
 * 
 * <p>Herní smyčka scény běží v samostatném vlákně a je třeba ji spustit
 * voláním metody {@linkplain #start()}. Zastavení pak provede metoda
 * {@linkplain #stop()}.</p>
 * 
 * <p>Vykreslování neprobíhá neustále, ale pouze v případě, že došlo ve scéně k
 * nějaké změně.</p>
 * 
 * @author Jan Šmucr
 */
public final class Scene extends GameCanvas implements Runnable
{

    public static final int COMMAND_LEFT = -6;
    public static final int COMMAND_MIDDLE = -5;
    public static final int COMMAND_RIGHT = -7;
    public static final int KEYPAD_UP = -1;
    public static final int KEYPAD_DOWN = -2;
    public static final int KEYPAD_LEFT = -3;
    public static final int KEYPAD_RIGHT = -4;
    public static final int NUMPAD_0 = 48;
    public static final int NUMPAD_1 = 49;
    public static final int NUMPAD_2 = 50;
    public static final int NUMPAD_3 = 51;
    public static final int NUMPAD_4 = 52;
    public static final int NUMPAD_5 = 53;
    public static final int NUMPAD_6 = 54;
    public static final int NUMPAD_7 = 55;
    public static final int NUMPAD_8 = 56;
    public static final int NUMPAD_9 = 57;
    public static final int NUMPAD_ASTERISK = 42;
    public static final int NUMPAD_POUND = 35;

    private static final int BACKGROUND = 0xFAF8EF;
    private static final int NUMBER_MARGIN = 3;
    private static final int ROUNDNESS = 4;
    private final Vector listeners = new Vector(2);
    private final Vector sceneElements;
    private final Number[][] numbers;
    private final Grid grid;
    private final Score score;
    private final Score best;
    private Dialog dialog;
    private final int spacing;
    private volatile boolean paused;
    private volatile boolean stopped;
    private volatile boolean animating;
    private long lastTime;
    private boolean sceneChanged = true;

    public Scene(final int rows, final int cols) throws IOException
    {
        super(false);

        sceneElements = new Vector(rows * cols + 1);

        numbers = new Number[rows][];
        for (int row = 0; row < rows; row++)
        {
            numbers[row] = new Number[cols];
        }

        grid = new Grid(0, 0, rows, cols, Number.WIDTH, Number.HEIGHT, NUMBER_MARGIN, ROUNDNESS);
        grid.setX((getWidth() - grid.getWidth()) / 2);
        //spacing = grid.getX();
        spacing = 5;

        best = new Score(grid.getX() + grid.getWidth(), spacing, ROUNDNESS, true);
        score = new Score(0, spacing, ROUNDNESS, false);
        updateScorePosition();

        final Logo logo = new Logo(grid.getX(), spacing);
        grid.setY(score.getY() + score.getHeight() + spacing);

        addToScene(logo);
        addToScene(best);
        addToScene(score);
        addToScene(grid);
        addToScene(new ActionsPane(this, spacing));
    }

    private void addToScene(final Object object)
    {
        if (!sceneElements.isEmpty() && (sceneElements.lastElement() instanceof Message))
        {
            sceneElements.insertElementAt(object, sceneElements.size() - 1);
        } else
        {
            sceneElements.addElement(object);
        }
        sceneChanged = true;
    }

    private void removeFromScene(final Object object)
    {
        sceneElements.removeElement(object);
        sceneChanged = true;
    }

    /**
     * Spustí herní smyčku.
     */
    public final void start()
    {
        lastTime = System.currentTimeMillis();
        if (paused && !stopped)
        {
            paused = false;
        } else
        {
            paused = false;
            stopped = false;
            new Thread(this).start();
        }
    }

    /**
     * Pozastaví herní smyčku.
     */
    public final void pause()
    {
        paused = true;
    }

    /**
     * Zastaví herní smyčku.
     */
    public final void stop()
    {
        stopped = true;
    }

    /**
     * @see Runnable#run() 
     */
    public final void run()
    {
        while (!stopped)
        {
            if (paused)
            {
                sleep();
                continue;
            }

            if (!(animating || sceneChanged))
            {
                Thread.yield();
            }

            animating = animate() > 0;
            sceneChanged |= animating;

            if (sceneChanged)
            {
                draw();
                sceneChanged = false;
            }
        }
    }

    protected final void keyPressed(int keyCode)
    {
        super.keyPressed(keyCode);
        
        final int gameKeyCode = getGameAction(keyCode);
        notifyKeyPressed(gameKeyCode == 0 ? keyCode : gameKeyCode);
    }

    private void sleep()
    {
        try
        {
            Thread.sleep(500);
        } catch (final InterruptedException ex)
        {

        }
    }

    /**
     * Vytvoří ve scéně na desce políčko s číslem.
     * @param number Číslo (mocnina 2 od 2 do 65536).
     * @param row Řádek na desce.
     * @param col Sloupec na desce.
     * @param runOnDone Pokud není <code>null</code>, bude provedeno po ukončení
     * animace.
     */
    public final void createNumber(final int number, final int row, final int col, final Runnable runOnDone)
    {
        final Number newNumber = new Number(number, grid.colToX(col), grid.rowToY(row), ROUNDNESS);
        final Number oldNumber = numbers[row][col];
        numbers[row][col] = newNumber;

        synchronized (sceneElements)
        {
            addToScene(newNumber);
        }

        if (oldNumber == null)
        {
            newNumber.appear(runOnDone);
        } else
        {
            newNumber.appear(new Runnable()
            {
                public final void run()
                {
                    synchronized (sceneElements)
                    {
                        removeFromScene(oldNumber);
                    }
                    if (runOnDone != null)
                    {
                        runOnDone.run();
                    }
                }
            });
        }
    }

    private void ensureSceneOrder(final Object topObject, final Object bottomObject)
    {
        int topIndex = -1;
        synchronized (sceneElements)
        {
            for (int i = 0; i < sceneElements.size(); i++)
            {
                if (sceneElements.elementAt(i) == topObject)
                {
                    topIndex = i;
                }
                if (sceneElements.elementAt(i) == bottomObject)
                {
                    if (topIndex != -1)
                    {
                        sceneElements.setElementAt(topObject, i);
                        sceneElements.setElementAt(bottomObject, topIndex);
                        sceneChanged = true;
                    }
                    return;
                }
            }
        }
    }

    /**
     * Provede ve scéně posun čísla po herní desce.
     * @param board Herní deska.
     * @param movement Pohyb.
     * @param runOnDone Pokud není <code>null</code>, provede se po dokončení
     * animace.
     */
    public final void move(final Board board, final Movement movement, final Runnable runOnDone)
    {
        final Number targetNumber = numbers[movement.destinationRow][movement.destinationCol];
        final Number sourceNumber = numbers[movement.sourceRow][movement.sourceCol];
        numbers[movement.destinationRow][movement.destinationCol] = sourceNumber;
        numbers[movement.sourceRow][movement.sourceCol] = null;
        ensureSceneOrder(targetNumber, sourceNumber);

        sourceNumber.move(getMovementPixelDistance(movement), movement.direction, new Runnable()
        {
            public final void run()
            {
                if (targetNumber != null)
                {
                    synchronized (sceneElements)
                    {
                        removeFromScene(targetNumber);
                    }
                }
                if (runOnDone != null)
                {
                    runOnDone.run();
                }
            }
        });
    }

    private int getMovementPixelDistance(final Movement movement)
    {
        switch (movement.direction)
        {
            case Direction.LEFT:
                return grid.colToX(movement.sourceCol) - grid.colToX(movement.destinationCol);
            case Direction.UP:
                return grid.rowToY(movement.sourceRow) - grid.rowToY(movement.destinationRow);
            case Direction.RIGHT:
                return grid.colToX(movement.destinationCol) - grid.colToX(movement.sourceCol);
            case Direction.DOWN:
                return grid.rowToY(movement.destinationRow) - grid.rowToY(movement.sourceRow);
            default:
                return 0;
        }
    }

    private void notifyKeyPressed(final int key)
    {
        for (int i = 0; i < listeners.size(); i++)
        {
            Object element = listeners.elementAt(i);
            if (element instanceof BoardChangeListener)
            {
                ((UserActivityListener) element).onKeyPressed(key);
            }
        }
    }

    /**
     * Přidá posluchače uživatelského vstupu.
     * @param listener Posluchač.
     */
    public final void addListener(final UserActivityListener listener)
    {
        listeners.addElement(listener);
    }

    /**
     * Odebere posluchače uživatelského vstupu.
     * @param listener Posluchač.
     */
    public final void removeListener(final UserActivityListener listener)
    {
        listeners.removeElement(listener);
    }

    /**
     * Aktualizuje skóre zobrazené ve scéně.
     * @param score Skóre.
     */
    public final void updateScore(final int score)
    {
        this.score.setScore(score);
        sceneChanged = true;
    }

    /**
     * @return <code>true</code>, pokud ve scéně probíhá nějaká animace.
     */
    public final boolean isAnimating()
    {
        return animating;
    }

    private void draw()
    {
        final Graphics g = getGraphics();

        g.setColor(BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());

        synchronized (sceneElements)
        {
            for (int i = 0; i < sceneElements.size(); i++)
            {
                final Object element = sceneElements.elementAt(i);
                if (element instanceof Drawable)
                {
                    ((Drawable) element).draw(g);
                }
            }
        }

        flushGraphics();
    }

    private int animate()
    {
        final long time = System.currentTimeMillis();
        final long msec = time - lastTime;
        int animatingCount = 0;

        synchronized (sceneElements)
        {
            for (int i = 0; i < sceneElements.size(); i++)
            {
                final Object element = sceneElements.elementAt(i);
                if (element instanceof Animable)
                {
                    final Animable animable = (Animable) element;
                    if (animable.animate(msec))
                    {
                        animatingCount++;
                    }
                }
            }
        }

        lastTime = time;
        return animatingCount;
    }

    /**
     * Aktualizuje nejlepší skóre zobrazené ve scéně.
     * @param best Skóre.
     */
    public final void updateBest(final int best)
    {
        this.best.setScore(best);
        updateScorePosition();
    }

    private void updateScorePosition()
    {
        score.setX(grid.getX() + grid.getWidth() - spacing - best.getWidth());
    }

    private void showDialog(final int type, final Runnable runOnDone)
    {
        if (dialog != null)
        {
            hideDialog(new Runnable()
            {
                public final void run()
                {
                    showDialog(type, runOnDone);
                }
            });
            return;
        }
        try
        {
            dialog = new Dialog(this, type);
            synchronized (sceneElements)
            {
                sceneElements.addElement(dialog);
            }
            dialog.appear(runOnDone);
        } catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Skryje právě zobrazený dialog.
     * @param runOnDone Pokud není <code>null</code>, bude provedeno po
     * dokončení animace.
     */
    public final void hideDialog(final Runnable runOnDone)
    {
        if (dialog == null)
        {
            return;
        }
        if (dialog.isVisible() && !(dialog.isAppearing() || dialog.isDisappearing()))
        {
            dialog.disappear(new Runnable()
            {
                public final void run()
                {
                    dialog = null;
                    synchronized (sceneElements)
                    {
                        removeFromScene(dialog);
                    }
                    if (runOnDone != null)
                    {
                        runOnDone.run();
                    }
                }
            });
            return;
        }
        synchronized (sceneElements)
        {
            removeFromScene(dialog);

        }
        dialog = null;
        if (runOnDone != null)
        {
            runOnDone.run();
        }
    }

    /**
     * @return <code>true</code>, pokud je zobrazen dialog oznamující konec hry.
     */
    public final boolean isGameOverDialogDisplayed()
    {
        return (dialog != null) && (dialog.getType() == Dialog.GAME_OVER);
    }

    /**
     * @return <code>true</code>, pokud je zobrazen libovolný dialog.
     */
    public final boolean isAnyDialogVisible()
    {
        return dialog != null;
    }
    
    private void setDialogVisible(final int type, final boolean visible, final Runnable runOnDone)
    {
        if (visible)
        {
            if (dialog != null)
            {
                if (dialog.getType() == type)
                {
                    if (runOnDone != null)
                    {
                        runOnDone.run();
                    }
                    return;
                }
                hideDialog(new Runnable()
                {
                    public final void run()
                    {
                        setDialogVisible(type, visible, runOnDone);
                    }
                });
                return;
            }
            showDialog(type, runOnDone);
            return;
        }
        if (dialog != null)
        {
            if (dialog.getType() == type)
            {
                hideDialog(runOnDone);
                return;
            }
        }
        if (runOnDone != null)
        {
            runOnDone.run();
        }
    }

    /**
     * Nastaví zobrazení dialogu oznamujícího konec hry.
     * @param displayed Zda má či nemá být zobrazen.
     * @param runOnDone Pokud není <code>null</code>, bude provedeno po
     * dokončení animace.
     */
    public final void setGameOverDialogDisplayed(final boolean displayed, final Runnable runOnDone)
    {
        setDialogVisible(Dialog.GAME_OVER, displayed, runOnDone);
    }

    /**
     * Odstraní políčko s číslem z desky (bez animace).
     * @param row Řádek.
     * @param col Sloupec.
     */
    public final void removeNumber(final int row, final int col)
    {
        final Number number = numbers[row][col];
        if (number != null)
        {
            synchronized (sceneElements)
            {
                removeFromScene(number);
            }
        }
    }

    /**
     * Nastaví zobrazení dialogu vytvoření nové hry.
     * @param displayed Zda má či nemá být zobrazen.
     * @param runOnDone Pokud není <code>null</code>, bude provedeno po
     * dokončení animace.
     */
    public final void setNewGameDialogDisplayed(final boolean displayed, final Runnable runOnDone)
    {
        setDialogVisible(Dialog.START_NEW_GAME, displayed, runOnDone);
    }
    
    /**
     * @return <code>true</code>, pokud je zobrazen dialog vytvoření nové hry.
     */
    public final boolean isNewGameDialogDisplayed()
    {
        return (dialog != null) && (dialog.getType() == Dialog.START_NEW_GAME);
    }
    
    /**
     * Nastaví zobrazení dialogu zobrazujícího gratulaci.
     * @param displayed Zda má či nemá být zobrazen.
     * @param runOnDone Pokud není <code>null</code>, bude provedeno po
     * dokončení animace.
     */
    public final void setCongratulationsDialogDisplayed(final boolean displayed, final Runnable runOnDone)
    {
        setDialogVisible(Dialog.CONGRATULATIONS, displayed, runOnDone);
    }
    
    /**
     * @return <code>true</code>, pokud je zobrazen dialog gratulace.
     */
    public final boolean isCongratulationsDialogDisplayed()
    {
        return (dialog != null) && (dialog.getType() == Dialog.CONGRATULATIONS);
    }
}

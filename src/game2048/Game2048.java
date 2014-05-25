package game2048;

import game2048.scene.Scene;
import game2048.scene.UserActivityListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.*;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 * <p>Jádro hry 2048. Cílem hry je docílit na herní desce získání čísla 2048
 * pomocí tahů ve čtyřech směrech, které způsobí posun a případné sečtení dvou
 * stejných čísel v řádku, příp. sloupci.</p>
 * 
 * <p>Tato třída slouží jako controller aplikace. Má jednu vazbu na vrstvu
 * datovou (třída {@linkplain Board}) a jednu vazbu na vrstvu prezentační
 * (třída {@linkplain Scene}). Z prezentační vrstvy přebírá informace o aktivitě
 * uživatele, z datové vrstvy informace o změně stavu herní desky.</p>
 * 
 * @author Jan Šmucr
 */
public final class Game2048 extends MIDlet implements BoardChangeListener, UserActivityListener
{

    private RecordStore store;
    private Display display;
    private Board board;
    private Scene scene;
    private int score;
    private int best;
    private boolean improvedBest;
    private volatile boolean canCreateNumber = false;

    /**
     * Metoda vyvolaná při přechodu aplikace do stavu Active.
     */
    public final void startApp()
    {
        display = Display.getDisplay(this);

        prepareBoard();
        prepareScene();
        displayScene();

        loadSettings();

        scene.start();
    }

    private void prepareBoard()
    {
        board = new Board();
        board.addListener(this);
    }

    private void prepareScene()
    {
        try
        {
            scene = new Scene(board.getRows(), board.getCols());
            scene.addListener(this);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void displayScene()
    {
        display.setCurrent(scene);
        scene.setFullScreenMode(true);
    }

    private boolean openStore()
    {
        try
        {
            store = RecordStore.openRecordStore("2048", true);
            return true;
        } catch (final RecordStoreException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private void closeStore()
    {
        try
        {
            store.closeRecordStore();
        } catch (final RecordStoreException e)
        {
            e.printStackTrace();
        }
    }

    private void loadSettings()
    {
        boolean loaded = false;
        if (openStore())
        {
            try
            {
                if (store.getNumRecords() > 0)
                {
                    final RecordEnumeration e = store.enumerateRecords(null, null, false);
                    final ByteArrayInputStream byteStream = new ByteArrayInputStream(e.nextRecord());
                    final DataInputStream dataStream = new DataInputStream(byteStream);
                    setBest(dataStream.readInt());
                    setScore(dataStream.readInt());
                    board.loadState(dataStream);
                    loaded = true;
                    dataStream.close();
                    byteStream.close();
                }
            } catch (final Exception e)
            {
                e.printStackTrace();
            }
            closeStore();
        }

        if (!loaded)
        {
            setBest(0);
            resetGame();
            return;
        }

        if (board.isEmpty())
        {
            resetGame();
        }
    }

    private void saveSettings()
    {
        if (openStore())
        {
            try
            {
                final int recordId;
                final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                final DataOutputStream dataStream = new DataOutputStream(byteStream);
                dataStream.writeInt(best);
                dataStream.writeInt(score);
                board.saveState(dataStream);
                final byte[] data = byteStream.toByteArray();
                dataStream.close();
                byteStream.close();
                if (store.getNumRecords() > 0)
                {
                    final RecordEnumeration e = store.enumerateRecords(null, null, false);
                    recordId = e.nextRecordId();
                    store.setRecord(recordId, data, 0, data.length);
                } else
                {
                    store.addRecord(data, 0, data.length);
                }
            } catch (final Exception e)
            {
                e.printStackTrace();
            }
            closeStore();
        }
    }

    /**
     * Metoda volaná při přechodu aplikace do stavu Paused. Ta provede de-facto
     * totéž jako by se aplikace vypínala, protože při přepnutí zpět do stavu
     * aktivního je stejně volána metoda {@linkplain #startApp()}, která provede
     * inicializaci aplikace znovu.
     */
    public final void pauseApp()
    {
        destroyApp(false);
    }

    /**
     * Uloží aktuální stav aplikace, umožní uvolnění prostředků a na displej
     * nastaví prázdné nečinné plátno. Tato metoda je volána i při přechodu do
     * stavu Paused.
     * @param unconditional <code>true</code>, pokud jde o bezpodmínečné
     * (nepřerušitelné) vypínání aplikace. Nepoužíváno.
     */
    public final void destroyApp(final boolean unconditional)
    {
        if (scene != null)
        {
            saveSettings();
            display.setCurrent(new EmptyCanvas());
            scene.stop();
            board = null;
            scene = null;
        }
    }

    private void resetGame()
    {
        improvedBest = false;
        setScore(0);
        board.init();
    }

    /**
     * @see BoardChangeListener#onNumberCreated(game2048.Board, int, int, boolean) 
     */
    public final void onNumberCreated(final Board board, final int row, final int col, final boolean last)
    {
        canCreateNumber = false;
        if (last && !board.canMove())
        {
            scene.createNumber(board.getNumber(row, col), row, col, new Runnable()
            {
                public final void run()
                {
                    scene.setGameOverDialogDisplayed(true, null);
                }
            });
        } else
        {
            scene.createNumber(board.getNumber(row, col), row, col, null);
        }
    }

    /**
     * @see BoardChangeListener#onNumbersJoined(game2048.Board, game2048.Movement)
     */
    public final void onNumbersJoined(final Board board, final Movement movement)
    {
        final int number = board.getNumber(movement.destinationRow, movement.destinationCol);
        setScore(score + number);

        scene.move(board, movement, new Runnable()
        {
            public void run()
            {
                synchronized (board)
                {
                    if (number == 2048)
                    {
                        scene.createNumber(number, movement.destinationRow, movement.destinationCol, new Runnable()
                        {
                            public final void run()
                            {
                                scene.setCongratulationsDialogDisplayed(true, null);
                            }
                        });
                    } else
                    {
                        scene.createNumber(number, movement.destinationRow, movement.destinationCol, null);
                    }

                    if (canCreateNumber)
                    {
                        board.createNumber();
                    }
                }
            }
        });
    }

    /**
     * @see BoardChangeListener#onNumberMoved(game2048.Board, game2048.Movement) 
     */
    public final void onNumberMoved(final Board board, final Movement movement)
    {
        scene.move(board, movement, new Runnable()
        {
            public void run()
            {
                synchronized (board)
                {
                    if (canCreateNumber)
                    {
                        board.createNumber();
                    }
                }
            }
        });
    }

    /**
     * @see UserActivityListener#onKeyPressed(int)
     */
    public final void onKeyPressed(final int key)
    {
        if (scene.isAnimating())
        {
            return;
        }

        if (scene.isGameOverDialogDisplayed() || scene.isNewGameDialogDisplayed())
        {
            switch (key)
            {
                case Scene.COMMAND_LEFT:
                case Scene.NUMPAD_ASTERISK:
                    scene.hideDialog(new Runnable()
                    {
                        public void run()
                        {
                            resetGame();
                        }
                    });
                    break;
                case Scene.COMMAND_RIGHT:
                case Scene.NUMPAD_POUND:
                    scene.hideDialog(null);
                    break;
            }
            return;
        }

        if (scene.isCongratulationsDialogDisplayed())
        {
            switch (key)
            {
                case Scene.COMMAND_LEFT:
                case Scene.NUMPAD_ASTERISK:
                    scene.hideDialog(null);
                    break;
                case Scene.COMMAND_RIGHT:
                case Scene.NUMPAD_POUND:
                    scene.setNewGameDialogDisplayed(true, null);
                    break;
            }
            return;
        }

        switch (key)
        {
            case Scene.COMMAND_LEFT:
            case Scene.NUMPAD_ASTERISK:
                exit();
                break;
            case Scene.COMMAND_RIGHT:
            case Scene.NUMPAD_POUND:
                scene.setNewGameDialogDisplayed(true, null);
                break;
            case Scene.KEYPAD_LEFT:
            case Scene.NUMPAD_4:
                canCreateNumber = board.move(Direction.LEFT);
                break;
            case Scene.KEYPAD_DOWN:
            case Scene.NUMPAD_8:
                canCreateNumber = board.move(Direction.DOWN);
                break;
            case Scene.KEYPAD_UP:
            case Scene.NUMPAD_2:
                canCreateNumber = board.move(Direction.UP);
                break;
            case Scene.KEYPAD_RIGHT:
            case Scene.NUMPAD_6:
                canCreateNumber = board.move(Direction.RIGHT);
                break;
        }
    }

    /**
     * @see BoardChangeListener#onNumberRemoved(game2048.Board, int, int)
     */
    public final void onNumberRemoved(final Board board, final int row, final int col)
    {
        scene.removeNumber(row, col);
    }

    private void setScore(final int score)
    {
        this.score = score;
        scene.updateScore(score);
        if (score > best)
        {
            improvedBest = true;
            setBest(score);
        }
    }

    private void setBest(final int best)
    {
        this.best = best;
        scene.updateBest(best);
    }

    private void exit()
    {
        destroyApp(true);
        notifyDestroyed();
    }
}

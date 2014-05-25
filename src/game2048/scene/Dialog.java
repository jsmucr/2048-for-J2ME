package game2048.scene;

import java.io.IOException;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Zobrazuje na plátně jeden z předvolených dialogů, na které lze odpovědět ano
 * nebo ne.
 *
 * @author Jan Šmucr
 */
public final class Dialog extends Message
{

    public static final int GAME_OVER = 0;
    public static final int CONGRATULATIONS = 1;
    public static final int START_NEW_GAME = 2;
    private final Image messageImage;
    private final Image yesImage;
    private final Image noImage;
    private final int type;

    public Dialog(final Canvas canvas, final int type) throws IOException
    {
        super(canvas);
        this.type = type;

        final String imageName;

        switch (type)
        {
            case GAME_OVER:
                imageName = "game-over";
                break;
            case CONGRATULATIONS:
                imageName = "congratulations";
                break;
            case START_NEW_GAME:
                imageName = "start-new-game";
                break;
            default:
                throw new IllegalArgumentException("type");
        }

        messageImage = Image.createImage(getClass().getResourceAsStream("images/text/" + imageName + ".png"));
        yesImage = Image.createImage(getClass().getResourceAsStream("images/text/yes.png"));
        noImage = Image.createImage(getClass().getResourceAsStream("images/text/no.png"));
    }

    /**
     * @see Message#drawContent(javax.microedition.lcdui.Graphics)
     */
    protected final void drawContent(final Graphics graphics)
    {
        graphics.drawImage(messageImage, 0, 0, Graphics.LEFT | Graphics.TOP);
    }

    /**
     * @see Message#getContentHeight()
     */
    protected final int getContentHeight()
    {
        return messageImage.getHeight();
    }

    /**
     * @see Message#drawOptionPane(javax.microedition.lcdui.Graphics)
     */
    protected final void drawOptionPane(final Graphics graphics)
    {
        graphics.drawImage(yesImage, 0, 0, Graphics.LEFT | Graphics.TOP);
        graphics.drawImage(noImage, getContentWidth() - noImage.getWidth(), 0, Graphics.LEFT | Graphics.TOP);
    }

    /**
     * @see Message#getOptionPaneHeight()
     */
    protected final int getOptionPaneHeight()
    {
        return Math.max(yesImage.getHeight(), noImage.getHeight());
    }

    /**
     * @return Typ dialogu ({@link #START_NEW_GAME}, {@link #GAME_OVER},
     * {@link #CONGRATULATIONS}).
     */
    public final int getType()
    {
        return type;
    }
}

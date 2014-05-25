package game2048.scene;

import java.io.IOException;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Třída vykreslující na spodní část plátna obrázky s nápisy Exit a New game.
 * 
 * @author Jan Šmucr
 */
public final class ActionsPane implements Drawable
{
    private final Image exitImage;
    private final Image newGameImage;
    private final int canvasPadding;
    private final Canvas canvas;
    private final int exitYOffset;
    private final int newGameXOffset;
    private final int newGameYOffset;
    
    public ActionsPane(final Canvas canvas, final int canvasPadding) throws IOException
    {
        exitImage = Image.createImage(getClass().getResourceAsStream("images/text/exit.png"));
        newGameImage = Image.createImage(getClass().getResourceAsStream("images/text/new-game.png"));
        this.canvas = canvas;
        this.canvasPadding = canvasPadding;
        this.exitYOffset = canvasPadding + exitImage.getHeight();
        this.newGameXOffset = canvasPadding + newGameImage.getWidth();
        this.newGameYOffset = canvasPadding + newGameImage.getHeight();
    }
    
    /**
     * @see Drawable#draw(javax.microedition.lcdui.Graphics) 
     */
    public final void draw(final Graphics graphics)
    {
        graphics.drawImage(exitImage, canvasPadding, canvas.getHeight() - exitYOffset, Graphics.LEFT | Graphics.TOP);
        graphics.drawImage(newGameImage, canvas.getWidth() - newGameXOffset, canvas.getHeight() - newGameYOffset, Graphics.LEFT | Graphics.TOP);
    }    
}

package game2048.scene;

import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Třída vykreslující buďto aktuální nebo nejlepší skóre ve scéně.
 *
 * @author Jan Šmucr
 */
public final class Score implements Drawable
{

    private static final int BACKGROUND = 0xBBADA0;
    private static final int NUMBER_SPACING = 0;
    private static final int PADDING = 4;

    private int x;
    private int y;
    private final int roundness;
    private final Image titleImage;
    private int score;
    private Dimensions textDimensions;
    private int width;
    private int height;
    private int titleX;
    private int titleY;
    private int textX;
    private int textY;

    public Score(final int x, final int y, final int roundness, final boolean best) throws IOException
    {
        this.x = x;
        this.y = y;
        this.roundness = roundness;

        titleImage = Image.createImage(getClass().getResourceAsStream("images/score/" + (best ? "best" : "score") + ".png"));
        setScore(0);
    }

    /**
     * Aktualizuje skóre.
     *
     * @param score Skóre.
     */
    public final void setScore(final int score)
    {
        this.score = score;
        textDimensions = ScoreDrawingTool.getScoreDimensions(score, NUMBER_SPACING);
        updatePositions();
    }

    private void updatePositions()
    {
        width = 2 * PADDING + Math.max(titleImage.getWidth(), textDimensions.width);
        height = 3 * PADDING + titleImage.getHeight() + textDimensions.height; //textDimensions.height;
        titleX = x - width + (width - titleImage.getWidth()) / 2;
        titleY = y + PADDING;
        textX = x - width + (width - textDimensions.width) / 2;
        textY = titleY + titleImage.getHeight() + PADDING;
    }

    /**
     * @see Drawable#draw(javax.microedition.lcdui.Graphics) 
     */
    public final void draw(final Graphics graphics)
    {
        graphics.setColor(BACKGROUND);
        graphics.fillRoundRect(x - width, y, width, height, roundness, roundness);

        graphics.drawImage(titleImage, titleX, titleY, Graphics.LEFT | Graphics.TOP);
        ScoreDrawingTool.drawScore(score, textX, textY, NUMBER_SPACING, graphics);
    }

    /**
     * @return Souřadnice X levého horního rohu v pixelech.
     */
    public final int getX()
    {
        return x;
    }

    /**
     * @return Souřadnice Y levého horního rohu v pixelech.
     */
    public final int getY()
    {
        return y;
    }

    /**
     * Nastaví souřadnici X levého horního rohu.
     * @param x Souřadnice X.
     */
    public final void setX(final int x)
    {
        this.x = x;
        updatePositions();
    }

    /**
     * Nastaví souřadnici Y levého horního rohu.
     * @param y Souřadnice Y.
     */
    public final void setY(final int y)
    {
        this.y = y;
        updatePositions();
    }

    /**
     * @return Šířka v pixelech.
     */
    public final int getWidth()
    {
        return width;
    }

    /**
     * @return Výška v pixelech.
     */
    public final int getHeight()
    {
        return height;
    }

}

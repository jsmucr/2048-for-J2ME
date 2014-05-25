package game2048.scene;

import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Utility třída umožňující vykreslit skóre, kde jsou jednotlivé číslice tvořené
 * obrázky.
 *
 * @author Jan Šmucr
 */
public final class ScoreDrawingTool
{
    private static final Image[] images = new Image[10];
    
    private ScoreDrawingTool()
    {
        
    }
    
    /**
     * Vrací velikost nápisu za daných podmínek.
     * @param score Skóre.
     * @param spacing Mezery mezi číslicemi v pixelech.
     * @return Velikost.
     */
    public static final Dimensions getScoreDimensions(final int score, final int spacing)
    {
        final char[] scoreString = Integer.toString(score).toCharArray();
        int width = 0;
        int height = 0;
        try
        {
            for (int i = 0; i < scoreString.length; i++)
            {
                final Image image = getImage(scoreString[i] - '0');
                width += image.getWidth();
                height = Math.max(height, image.getHeight());
            }
            width += (scoreString.length - 1) * spacing;
            return new Dimensions(width, height);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            return new Dimensions(0, 0);
        }
    }
    
    private static Image getImage(final int number) throws IOException
    {
        if (images[number] == null)
        {
            return images[number] = Image.createImage(ScoreDrawingTool.class.getResourceAsStream("images/score/" + number + ".png"));
        }
        return images[number];
    }
    
    /**
     * Vykreslí skóre.
     * @param score Skóre.
     * @param x Souřadnice X.
     * @param y Souřadnice Y.
     * @param spacing Mezery mezi číslicemi v pixelech.
     * @param graphics Cíl.
     */
    public static final void drawScore(final int score, int x, final int y, final int spacing, final Graphics graphics)
    {
        final char[] scoreString = Integer.toString(score).toCharArray();
        try
        {
            for (int i = 0; i < scoreString.length; i++)
            {
                final Image image = getImage(scoreString[i] - '0');
                graphics.drawImage(image, x, y, Graphics.LEFT | Graphics.TOP);
                x += image.getWidth();
                x += spacing;
            }
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }
}

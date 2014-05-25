package game2048.scene;

import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Vykreslí logo hry.
 * 
 * @author Jan Šmucr
 */
public final class Logo implements Drawable
{
    private final Image image;
    private int x;
    private int y;
    
    public Logo(final int x, final int y) throws IOException
    {
        image = Image.createImage(getClass().getResourceAsStream("images/logo.png"));
        this.x = x;
        this.y = y;
    }

    /**
     * @see Drawable#draw(javax.microedition.lcdui.Graphics) 
     */
    public final void draw(final Graphics graphics)
    {
        graphics.drawImage(image, x, y, Graphics.LEFT | Graphics.TOP);
    }
    
    /**
     * @return Šířka v pixelech.
     */    
    public final int getWidth()
    {
        return image.getWidth();
    }
    
    /**
     * @return Výška v pixelech.
     */    
    public final int getHeight()
    {
        return image.getHeight();
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
    }

    /**
     * Nastaví souřadnici Y levého horního rohu.
     * @param y Souřadnice Y.
     */
    public final void setY(final int y)
    {
        this.y = y;
    }
    
    
}

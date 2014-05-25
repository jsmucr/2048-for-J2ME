package game2048.scene;

import javax.microedition.lcdui.Graphics;

/**
 * Rozhraní implementované prvky, které se umějí vykreslit.
 * 
 * @author Jan Šmucr
 */
public interface Drawable
{
    /**
     * Vykreslí prvek.
     * @param graphics Cíl. 
     */
    void draw(Graphics graphics);
}

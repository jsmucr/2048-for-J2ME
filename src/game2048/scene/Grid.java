package game2048.scene;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Vykresluje prázdnou herní desku.
 * 
 * @author Jan Šmucr
 */
public final class Grid implements Drawable
{
    private static final int CELL_BACKGROUND = 0xCDC1B4;
    private static final int BORDER_COLOR = 0xBBADA0;
    private int x;
    private int y;
    private final int width;
    private final int height;
    private final int rows;
    private final int cols;
    private final int cellMargin;
    private final int cellWidth;
    private final int cellHeight;
    private final int roundness;
    private final Image gridImage;
    
    /* Precomputed values */
    private int cellDrawStartX;
    private int cellDrawStartY;
    private int cellDrawOffsetX;
    private int cellDrawOffsetY;
    
    public Grid(final int x, final int y, final int rows, final int cols, final int cellWidth, final int cellHeight, final int cellMargin, final int roundness)
    {
        this.x = x;
        this.y = y;
        this.rows = rows;
        this.cols = cols;
        this.cellMargin = cellMargin;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.roundness = roundness;
        this.width = 2 * cellMargin + (2 * cellMargin + cellWidth) * cols;
        this.height = 2 * cellMargin + (2 * cellMargin + cellHeight) * rows;
        
        updatePrecomputedValues();
        
        gridImage = Image.createImage(width, height);
        drawAt(0, 0, gridImage.getGraphics());
    }

    /**
     * @see Drawable#draw(javax.microedition.lcdui.Graphics)  
     */
    public final void draw(final Graphics graphics)
    {
        graphics.drawImage(gridImage, x, y, Graphics.LEFT | Graphics.TOP);
    }
    
    private void drawAt(final int x, final int y, final Graphics graphics)
    {
        graphics.setColor(BORDER_COLOR);
        graphics.fillRoundRect(x, y, width, height, roundness, roundness);
        
        graphics.setColor(CELL_BACKGROUND);
        for (int row = 0, cellY = cellDrawStartY; row < rows; row++, cellY += cellDrawOffsetY)
        {
            for (int col = 0, cellX = cellDrawStartX; col < cols; col++, cellX += cellDrawOffsetX)
            {
                graphics.fillRoundRect(cellX, cellY, cellWidth, cellHeight, roundness, roundness);
            }
        }
    }
    
    private void updatePrecomputedValues()
    {
        cellDrawStartX = x + 2 * cellMargin;
        cellDrawStartY = y + 2 * cellMargin;
        cellDrawOffsetX = cellWidth + 2 * cellMargin;
        cellDrawOffsetY = cellHeight + 2 * cellMargin;
    }
    
    /**
     * Převádí číslo sloupce na jeho vzdálenost od kraje scény v pixelech.
     * @param col Číslo sloupce.
     * @return Vzdálenost od levého okraje v pixelech.
     */
    public final int colToX(final int col)
    {
        return cellDrawStartX + col * cellDrawOffsetX;
    }

    /**
     * Převádí číslo řádky na jeho vzdálenost od kraje scény v pixelech.
     * @param row Číslo řádky.
     * @return Vzdálenost od horního okraje v pixelech.
     */
    public final int rowToY(final int row)
    {
        return cellDrawStartY + row * cellDrawOffsetY;
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
        updatePrecomputedValues();
    }

    /**
     * Nastaví souřadnici Y levého horního rohu.
     * @param y Souřadnice Y.
     */
    public final void setY(final int y)
    {
        this.y = y;
        updatePrecomputedValues();
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

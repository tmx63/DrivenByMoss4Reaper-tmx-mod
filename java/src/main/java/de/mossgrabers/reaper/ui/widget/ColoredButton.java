package de.mossgrabers.reaper.ui.widget;

import javax.swing.JButton;

import java.awt.Color;
import java.awt.Insets;


/**
 * Makes the button 'larger'.
 */
public class ColoredButton extends JButton
{
    private static final long serialVersionUID = 8282196961705550272L;


    /**
     * Constructor.
     */
    public ColoredButton ()
    {
        this.setText ("Click to pick");
        this.setContentAreaFilled (false);
        this.setOpaque (true);
        this.setMargin (new Insets (0, 0, 0, 0));
    }


    /** {@inheritDoc} */
    @Override
    public void setBackground (final Color color)
    {
        super.setBackground (color);
        this.setForeground (color);
    }
}

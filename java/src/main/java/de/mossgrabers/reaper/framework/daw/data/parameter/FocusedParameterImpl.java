// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.reaper.framework.daw.data.parameter;

import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.data.empty.EmptyParameter;
import de.mossgrabers.framework.observer.IValueObserver;
import de.mossgrabers.framework.parameter.IFocusedParameter;
import de.mossgrabers.framework.parameter.IParameter;
import de.mossgrabers.reaper.framework.daw.data.CursorDeviceImpl;
import de.mossgrabers.reaper.framework.daw.data.bank.ParameterBankImpl;


/**
 * Encapsulates the data of a parameter.
 *
 * @author Jürgen Moßgraber
 */
public class FocusedParameterImpl implements IFocusedParameter
{
    private final CursorDeviceImpl cursorDevice;


    /**
     * Constructor.
     *
     * @param cursorDevice The cursor device
     */
    public FocusedParameterImpl (final CursorDeviceImpl cursorDevice)
    {
        this.cursorDevice = cursorDevice;
    }


    /** {@inheritDoc} */
    @Override
    public void inc (final double increment)
    {
        this.getParam ().inc (increment);
    }


    /** {@inheritDoc} */
    @Override
    public String getDisplayedValue ()
    {
        return this.getParam ().getDisplayedValue ();
    }


    /** {@inheritDoc} */
    @Override
    public String getDisplayedValue (final int limit)
    {
        return this.getParam ().getDisplayedValue (limit);
    }


    /** {@inheritDoc} */
    @Override
    public int getValue ()
    {
        return this.getParam ().getValue ();
    }


    /** {@inheritDoc} */
    @Override
    public void setValue (final int value)
    {
        this.getParam ().setValue (value);
    }


    /** {@inheritDoc} */
    @Override
    public void setValue (final IValueChanger valueChanger, final int value)
    {
        this.getParam ().setValue (valueChanger, value);
    }


    /** {@inheritDoc} */
    @Override
    public void setNormalizedValue (final double value)
    {
        this.getParam ().setNormalizedValue (value);
    }


    /** {@inheritDoc} */
    @Override
    public void setValueImmediatly (final int value)
    {
        this.getParam ().setValueImmediatly (value);
    }


    /** {@inheritDoc} */
    @Override
    public void changeValue (final int value)
    {
        this.getParam ().changeValue (value);
    }


    /** {@inheritDoc} */
    @Override
    public void changeValue (final IValueChanger valueChanger, final int value)
    {
        this.getParam ().changeValue (valueChanger, value);
    }


    /** {@inheritDoc} */
    @Override
    public void resetValue ()
    {
        this.getParam ().resetValue ();
    }


    /** {@inheritDoc} */
    @Override
    public void touchValue (final boolean isBeingTouched)
    {
        this.getParam ().touchValue (isBeingTouched);
    }


    /** {@inheritDoc} */
    @Override
    public int getModulatedValue ()
    {
        return this.getParam ().getModulatedValue ();
    }


    /** {@inheritDoc} */
    @Override
    public void setIndication (final boolean enable)
    {
        this.getParam ().setIndication (enable);
    }


    /** {@inheritDoc} */
    @Override
    public int getNumberOfSteps ()
    {
        return this.getParam ().getNumberOfSteps ();
    }


    /** {@inheritDoc} */
    @Override
    public boolean doesExist ()
    {
        return this.getParam ().doesExist ();
    }


    /** {@inheritDoc} */
    @Override
    public int getIndex ()
    {
        return this.getParam ().getIndex ();
    }


    /** {@inheritDoc} */
    @Override
    public int getPosition ()
    {
        return this.getParam ().getPosition ();
    }


    /** {@inheritDoc} */
    @Override
    public boolean isSelected ()
    {
        return this.getParam ().isSelected ();
    }


    /** {@inheritDoc} */
    @Override
    public void setSelected (final boolean isSelected)
    {
        this.getParam ().setSelected (isSelected);
    }


    /** {@inheritDoc} */
    @Override
    public void select ()
    {
        this.getParam ().select ();
    }


    /** {@inheritDoc} */
    @Override
    public void toggleMultiSelect ()
    {
        this.getParam ().toggleMultiSelect ();
    }


    /** {@inheritDoc} */
    @Override
    public String getName ()
    {
        return this.getParam ().getName ();
    }


    /** {@inheritDoc} */
    @Override
    public String getName (final int limit)
    {
        return this.getParam ().getName (limit);
    }


    /** {@inheritDoc} */
    @Override
    public void addNameObserver (final IValueObserver<String> observer)
    {
        this.getParam ().addNameObserver (observer);
    }


    /** {@inheritDoc} */
    @Override
    public void setName (final String name)
    {
        this.getParam ().setName (name);
    }


    /** {@inheritDoc} */
    @Override
    public void enableObservers (final boolean enable)
    {
        this.getParam ().enableObservers (enable);
    }


    private IParameter getParam ()
    {
        if (this.cursorDevice.doesExist ())
        {
            final int paramIndex = this.cursorDevice.getLastTouchedParameterIndex ();
            if (paramIndex >= 0 && this.cursorDevice.getParameterBank () instanceof final ParameterBankImpl paramBankImpl && paramIndex < paramBankImpl.getUnpagedItemCount ())
                return paramBankImpl.getUnpagedItem (paramIndex);
        }
        return EmptyParameter.INSTANCE;
    }
}

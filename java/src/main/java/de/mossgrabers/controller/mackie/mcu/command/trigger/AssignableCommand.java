// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.mackie.mcu.command.trigger;

import de.mossgrabers.controller.mackie.mcu.MCUConfiguration;
import de.mossgrabers.controller.mackie.mcu.controller.MCUControlSurface;
import de.mossgrabers.framework.command.continuous.JogWheelCommand;
import de.mossgrabers.framework.command.trigger.FootswitchCommand;
import de.mossgrabers.framework.configuration.AbstractConfiguration;
import de.mossgrabers.framework.daw.IApplication;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.featuregroup.IMode;
import de.mossgrabers.framework.featuregroup.ModeManager;
import de.mossgrabers.framework.mode.MasterVolumeMode;
import de.mossgrabers.framework.mode.Modes;
import de.mossgrabers.framework.utils.ButtonEvent;


/**
 * Command for assignable functions.
 *
 * @author Jürgen Moßgraber
 */
public class AssignableCommand extends FootswitchCommand<MCUControlSurface, MCUConfiguration>
{
    private final JogWheelCommand<MCUControlSurface, MCUConfiguration>  jogWheelCommand;
    private final ModeSwitcher                                          switcher;
    private final MCUFlipCommand                                        flipCommand;
    private final MCUMoveTrackBankCommand                               previousTrackCommand;
    private final MCUMoveTrackBankCommand                               nextTrackCommand;
    private final MasterVolumeMode<MCUControlSurface, MCUConfiguration> masterVolumeMode;


    /**
     * Constructor.
     *
     * @param index The index of the assignable button
     * @param model The model
     * @param surface The surface
     * @param jogWheelCommand The jog-wheel command
     * @param masterVolumeMode
     */
    public AssignableCommand (final int index, final IModel model, final MCUControlSurface surface, final JogWheelCommand<MCUControlSurface, MCUConfiguration> jogWheelCommand, final MasterVolumeMode<MCUControlSurface, MCUConfiguration> masterVolumeMode)
    {
        super (model, surface, index);

        this.jogWheelCommand = jogWheelCommand;
        this.masterVolumeMode = masterVolumeMode;
        this.switcher = new ModeSwitcher (surface);
        this.flipCommand = new MCUFlipCommand (model, surface);

        this.previousTrackCommand = new MCUMoveTrackBankCommand (this.model, surface, true, true);
        this.nextTrackCommand = new MCUMoveTrackBankCommand (this.model, surface, true, false);
    }


    /** {@inheritDoc} */
    @Override
    public void execute (final ButtonEvent event, final int velocity)
    {
        final MCUConfiguration configuration = this.surface.getConfiguration ();

        switch (this.getSetting ())
        {
            case MCUConfiguration.FOOTSWITCH_PREV_MODE:
                if (event == ButtonEvent.DOWN)
                    this.switcher.scrollDown ();
                break;

            case MCUConfiguration.FOOTSWITCH_NEXT_MODE:
                if (event == ButtonEvent.DOWN)
                    this.switcher.scrollUp ();
                break;

            case MCUConfiguration.FOOTSWITCH_SHOW_MARKER_MODE:
                if (event != ButtonEvent.DOWN)
                    return;
                final ModeManager modeManager = this.surface.getModeManager ();
                if (modeManager.isActive (Modes.MARKERS))
                    modeManager.restore ();
                else
                    modeManager.setActive (Modes.MARKERS);
                final IMode mode = modeManager.getActive ();
                if (mode != null)
                    this.surface.getDisplay ().notify (mode.getName ());
                break;

            case MCUConfiguration.FOOTSWITCH_USE_FADERS_LIKE_EDIT_KNOBS:
                this.flipCommand.executeNormal (event);
                break;

            case MCUConfiguration.FOOTSWITCH_TOGGLE_MOTOR_FADERS_ON_OFF:
                if (event != ButtonEvent.DOWN)
                    return;
                configuration.toggleMotorFaders ();
                this.mvHelper.delayDisplay (() -> "Motor Faders: " + (configuration.hasMotorFaders () ? "On" : "Off"));
                break;

            case MCUConfiguration.FOOTSWITCH_PUNCH_IN:
                if (event == ButtonEvent.DOWN)
                    this.model.getTransport ().togglePunchIn ();
                break;

            case MCUConfiguration.FOOTSWITCH_PUNCH_OUT:
                if (event == ButtonEvent.DOWN)
                    this.model.getTransport ().togglePunchOut ();
                break;

            case MCUConfiguration.FOOTSWITCH_DEVICE_ON_OFF:
                if (event == ButtonEvent.DOWN)
                    this.model.getCursorDevice ().toggleEnabledState ();
                break;

            case MCUConfiguration.PREV_CHANNEL:
                this.previousTrackCommand.execute (event, velocity);
                break;

            case MCUConfiguration.NEXT_CHANNEL:
                this.nextTrackCommand.execute (event, velocity);
                break;

            case MCUConfiguration.CONTROL_LAST_PARAM_ENCODER:
                if (event == ButtonEvent.DOWN)
                    this.jogWheelCommand.toggleControlLastParamActive ();
                break;

            case MCUConfiguration.CONTROL_LAST_PARAM_MASTER_FADER:
                if (event == ButtonEvent.DOWN)
                    this.masterVolumeMode.toggleControlLastParamActive ();
                break;

            case MCUConfiguration.FOOTSWITCH_ACTION:
                if (event != ButtonEvent.DOWN)
                    return;
                final String assignableActionID = configuration.getAssignableAction (this.index);
                // [tmx mod] Diagnostic: an action named LEDPROBE starts/stops the LED probe
                if (assignableActionID != null && "LEDPROBE".equalsIgnoreCase (assignableActionID.trim ()))
                {
                    toggleLedProbe (this.surface);
                    return;
                }
                if (assignableActionID != null)
                    this.model.getApplication ().invokeAction (assignableActionID);
                break;

            default:
                super.execute (event, velocity);
                break;
        }
    }


    /** {@inheritDoc} */
    @Override
    protected int getSetting ()
    {
        return this.surface.getConfiguration ().getAssignable (this.index);
    }


    /**
     * Test if the assignable is active.
     *
     * @return True if active
     */
    public boolean isActive ()
    {
        switch (this.getSetting ())
        {
            case AbstractConfiguration.FOOTSWITCH_TOGGLE_PLAY:
                return this.model.getTransport ().isPlaying ();

            case AbstractConfiguration.FOOTSWITCH_TOGGLE_RECORD:
                return this.model.getTransport ().isRecording ();

            case AbstractConfiguration.FOOTSWITCH_TOGGLE_CLIP_OVERDUB:
                return this.model.getTransport ().isLauncherOverdub ();

            case AbstractConfiguration.FOOTSWITCH_PANEL_LAYOUT_ARRANGE:
                return IApplication.PANEL_LAYOUT_ARRANGE.equals (this.model.getApplication ().getPanelLayout ());

            case AbstractConfiguration.FOOTSWITCH_PANEL_LAYOUT_MIX:
                return IApplication.PANEL_LAYOUT_MIX.equals (this.model.getApplication ().getPanelLayout ());

            case AbstractConfiguration.FOOTSWITCH_PANEL_LAYOUT_EDIT:
                return IApplication.PANEL_LAYOUT_EDIT.equals (this.model.getApplication ().getPanelLayout ());

            case MCUConfiguration.FOOTSWITCH_SHOW_MARKER_MODE:
                return this.surface.getModeManager ().isActive (Modes.MARKERS);

            case MCUConfiguration.FOOTSWITCH_USE_FADERS_LIKE_EDIT_KNOBS:
                return this.surface.getConfiguration ().useFadersAsKnobs ();

            case MCUConfiguration.FOOTSWITCH_TOGGLE_MOTOR_FADERS_ON_OFF:
                return this.surface.getConfiguration ().hasMotorFaders ();

            case MCUConfiguration.FOOTSWITCH_PUNCH_IN:
                return this.model.getTransport ().isPunchInEnabled ();

            case MCUConfiguration.FOOTSWITCH_PUNCH_OUT:
                return this.model.getTransport ().isPunchOutEnabled ();

            case MCUConfiguration.FOOTSWITCH_DEVICE_ON_OFF:
                return this.model.getCursorDevice ().isEnabled ();

            case MCUConfiguration.CONTROL_LAST_PARAM_ENCODER:
                return this.jogWheelCommand.isControlLastParamActive ();

            case MCUConfiguration.CONTROL_LAST_PARAM_MASTER_FADER:
                return this.masterVolumeMode.isControlLastParamActive ();

            // [tmx mod] Light the button if the assigned Reaper action is toggled on
            case MCUConfiguration.FOOTSWITCH_ACTION:
                final String assignableActionID = this.surface.getConfiguration ().getAssignableAction (this.index);
                return assignableActionID != null && this.model.getApplication ().isActionActive (assignableActionID);

            case AbstractConfiguration.FOOTSWITCH_UNDO:
            case AbstractConfiguration.FOOTSWITCH_TAP_TEMPO:
            case AbstractConfiguration.FOOTSWITCH_NEW_BUTTON:
            case AbstractConfiguration.FOOTSWITCH_CLIP_BASED_LOOPER:
            case AbstractConfiguration.FOOTSWITCH_STOP_ALL_CLIPS:
            case AbstractConfiguration.FOOTSWITCH_ADD_INSTRUMENT_TRACK:
            case AbstractConfiguration.FOOTSWITCH_ADD_AUDIO_TRACK:
            case AbstractConfiguration.FOOTSWITCH_ADD_EFFECT_TRACK:
            case AbstractConfiguration.FOOTSWITCH_QUANTIZE:
            case MCUConfiguration.FOOTSWITCH_PREV_MODE:
            case MCUConfiguration.FOOTSWITCH_NEXT_MODE:
            case MCUConfiguration.PREV_CHANNEL:
            case MCUConfiguration.NEXT_CHANNEL:
            default:
                return false;
        }
    }


    // ---------------------------------------------------------------------------------------
    // [tmx mod] LED probe: lights every note (and CC) address on the controller one after the
    // other and shows the current address on the display, to find out which messages light a
    // specific button. Press the LEDPROBE button again to stop.
    // ---------------------------------------------------------------------------------------

    private static volatile Thread ledProbeThread;


    private static synchronized void toggleLedProbe (final MCUControlSurface surface)
    {
        final Thread running = ledProbeThread;
        if (running != null && running.isAlive ())
        {
            running.interrupt ();
            return;
        }
        final Thread thread = new Thread ( () -> runLedProbe (surface), "LED probe");
        thread.setDaemon (true);
        ledProbeThread = thread;
        thread.start ();
    }


    private static void runLedProbe (final MCUControlSurface surface)
    {
        final de.mossgrabers.framework.daw.midi.IMidiOutput output = surface.getMidiOutput ();
        try
        {
            // Pass 1: all notes on MIDI channel 1
            for (int note = 0; note < 128; note++)
            {
                probeNotify (surface, "LED probe: NOTE " + note);
                output.sendNoteEx (0, note, 127);
                Thread.sleep (600);
                output.sendNoteEx (0, note, 0);
            }

            // Pass 2: the F1-F8 notes on MIDI channels 2-16
            for (int channel = 1; channel < 16; channel++)
            {
                probeNotify (surface, "LED probe: F-NOTES on CHANNEL " + (channel + 1));
                for (int note = 0x36; note <= 0x3D; note++)
                    output.sendNoteEx (channel, note, 127);
                Thread.sleep (1500);
                for (int note = 0x36; note <= 0x3D; note++)
                    output.sendNoteEx (channel, note, 0);
            }

            // Pass 3: all CCs on MIDI channel 1
            for (int cc = 0; cc < 128; cc++)
            {
                probeNotify (surface, "LED probe: CC " + cc);
                output.sendCCEx (0, cc, 127);
                Thread.sleep (300);
                output.sendCCEx (0, cc, 0);
            }

            probeNotify (surface, "LED probe: DONE");
        }
        catch (final InterruptedException ex)
        {
            probeNotify (surface, "LED probe: STOPPED");
            Thread.currentThread ().interrupt ();
        }
        finally
        {
            // Restore all lights and displays to their normal state
            surface.forceFlush ();
        }
    }


    private static void probeNotify (final MCUControlSurface surface, final String message)
    {
        try
        {
            surface.getDisplay ().notify (message);
        }
        catch (final RuntimeException ex)
        {
            // Display is only a convenience, ignore
        }
    }
}

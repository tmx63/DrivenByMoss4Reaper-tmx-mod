// (c) 2017-2026
// Written by Jürgen Moßgraber - mossgrabers.de
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.reaper.framework;

import de.mossgrabers.reaper.ui.utils.LogModel;

import com.nikhaldimann.inieditor.IniEditor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


/**
 * Manages access to the different Reaper INI files.
 *
 * @author Jürgen Moßgraber
 */
public class IniFiles
{
    private static final String OPTION_FORMAT_NO_SPACES = "%s%s%s";
    private static final String FX_TAGS                 = "reaper-fxtags.ini";
    private static final String FX_FOLDERS              = "reaper-fxfolders.ini";
    private static final String PARAM_MAPS              = "DrivenByMoss4Reaper-ParameterMaps.ini";
    private static final String REAPER_MAIN             = "REAPER.ini";
    private static final String REAPER_MAIN2            = "reaper.ini";

    private final IniEditor     iniFxTags               = new IniEditor ();
    private final IniEditor     iniFxFolders            = new IniEditor ();
    private final IniEditor     iniDeviceMaps           = new IniEditor ();
    private final IniEditor     iniReaperMain           = new IniEditor ();

    private String              iniPath;
    private LogModel            logModel;

    private boolean             isFxTagsPresent;
    private boolean             isFxFoldersPresent;
    private boolean             isParamMapsPresent;
    private String              paramMapsFilename;
    private File                reaperINIFile;
    private long                reaperINILastChange     = -1;


    /**
     * Constructor.
     */
    public IniFiles ()
    {
        this.iniFxTags.setOptionFormatString (OPTION_FORMAT_NO_SPACES);
        this.iniFxFolders.setOptionFormatString (OPTION_FORMAT_NO_SPACES);
        this.iniDeviceMaps.setOptionFormatString (OPTION_FORMAT_NO_SPACES);
        this.iniReaperMain.setOptionFormatString (OPTION_FORMAT_NO_SPACES);
    }


    /**
     * Get the INI path.
     *
     * @return The INI path
     */
    public String getIniPath ()
    {
        return this.iniPath;
    }


    /**
     * Load all INI files.
     *
     * @param iniPath The path to the INI files
     * @param logModel Where to log errors
     */
    public void init (final String iniPath, final LogModel logModel)
    {
        this.iniPath = iniPath;
        this.logModel = logModel;

        this.reaperINIFile = new File (iniPath + File.separator + REAPER_MAIN);
        if (!this.reaperINIFile.exists ())
            this.reaperINIFile = new File (iniPath + File.separator + REAPER_MAIN2);
        this.loadReaperINI ();

        this.isFxTagsPresent = this.loadINIFile (iniPath + File.separator + FX_TAGS, this.iniFxTags);
        this.isFxFoldersPresent = this.loadINIFile (iniPath + File.separator + FX_FOLDERS, this.iniFxFolders);
        this.paramMapsFilename = iniPath + File.separator + PARAM_MAPS;
        this.isParamMapsPresent = this.loadINIFile (this.paramMapsFilename, this.iniDeviceMaps);
    }


    /**
     * Get the FX tags configuration file.
     *
     * @return The file
     */
    public IniEditor getIniFxTags ()
    {
        return this.iniFxTags;
    }


    /**
     * Get the FX folders configuration file.
     *
     * @return The file
     */
    public IniEditor getIniFxFolders ()
    {
        return this.iniFxFolders;
    }


    /**
     * Get the parameter maps configuration file.
     *
     * @return The file
     */
    public IniEditor getIniParamMaps ()
    {
        return this.iniDeviceMaps;
    }


    /**
     * Store the parameter maps configuration file.
     *
     * @throws IOException Could not store the file
     */
    public void storeIniParamMaps () throws IOException
    {
        this.iniDeviceMaps.save (this.paramMapsFilename);
    }


    /**
     * Is the FX tags configuration file present?
     *
     * @return True if successfully loaded
     */
    public boolean isFxTagsPresent ()
    {
        return this.isFxTagsPresent;
    }


    /**
     * Is the FX folders configuration file present?
     *
     * @return True if successfully loaded
     */
    public boolean isFxFoldersPresent ()
    {
        return this.isFxFoldersPresent;
    }


    /**
     * Is the parameter maps configuration file present?
     *
     * @return True if successfully loaded
     */
    public boolean isParamMapsPresent ()
    {
        return this.isParamMapsPresent;
    }


    /**
     * Get an option value from the main INI file as an integer.
     *
     * @param section The section in the INI file
     * @param option The option name
     * @param defaultValue The default value to return if the value could not be read
     * @return The value
     */
    public int getMainIniInteger (final String section, final String option, final int defaultValue)
    {
        String value;
        synchronized (this.iniReaperMain)
        {
            this.loadReaperINI ();

            try
            {
                value = this.iniReaperMain.get (section, option);
            }
            catch (final com.nikhaldimann.inieditor.IniEditor.NoSuchSectionException ex)
            {
                return defaultValue;
            }
        }
        if (value == null)
            return defaultValue;
        try
        {
            return Integer.parseInt (value);
        }
        catch (final NumberFormatException ex)
        {
            return defaultValue;
        }
    }


    private void loadReaperINI ()
    {
        synchronized (this.iniReaperMain)
        {
            final long modified = this.reaperINIFile.lastModified ();
            if (this.reaperINILastChange != modified)
            {
                this.reaperINILastChange = modified;
                this.loadINIFile (this.reaperINIFile, this.iniReaperMain);
            }
        }
    }


    /**
     * Load an INI file.
     *
     * @param filename The absolute filename to try
     * @param iniFile The INI file
     * @return True if successfully loaded
     */
    private boolean loadINIFile (final String filename, final IniEditor iniFile)
    {
        return this.loadINIFile (new File (filename), iniFile);
    }


    /**
     * Load an INI file.
     *
     * @param file The file to load
     * @param iniFile The INI file
     * @return True if successfully loaded
     */
    private boolean loadINIFile (final File file, final IniEditor iniFile)
    {
        try
        {
            if (file.exists ())
            {
                try (final FileReader reader = new FileReader (file.getAbsolutePath (), StandardCharsets.UTF_8))
                {
                    iniFile.load (reader);
                }
                return true;
            }

            this.logModel.info (file.getName () + " not present (this is not an error!).");
            return false;
        }
        catch (final IOException ex)
        {
            this.logModel.error ("Could not load file: " + file.getName (), ex);
            return false;
        }
    }
}
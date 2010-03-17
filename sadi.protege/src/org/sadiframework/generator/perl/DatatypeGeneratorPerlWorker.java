/**
 * 
 */
package org.sadiframework.generator.perl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;

import org.jdesktop.swingworker.SwingWorker;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.sadiframework.editor.views.SADIPreferencePanel;
import org.sadiframework.editor.views.SadiServiceGeneratorView;
import org.sadiframework.preferences.PreferenceManager;

/**
 * @author Eddie
 * 
 */
public class DatatypeGeneratorPerlWorker extends SwingWorker {

    private String filename;
    private PreferenceManager manager = PreferenceManager.newInstance();

    public DatatypeGeneratorPerlWorker(String filename) {

        this.filename = filename == null ? "" : filename;
        // listen for changes to the DO_SERVICE_GENERATION preference
        manager.addPropertyChangeListener(SadiServiceGeneratorView.DO_DATATYPE_GENERATION,
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        // cancel our task
                        if (!manager.getBooleanPreference(
                                SadiServiceGeneratorView.DO_DATATYPE_GENERATION, false)) {
                            if (!isCancelled() || !isDone()) {
                                cancel(true);
                            }
                        }
                    }
                });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jdesktop.swingworker.SwingWorker#doInBackground()
     */
    @Override
    protected Object doInBackground() {

        String perl = manager.getPreference(SADIPreferencePanel.PERL_PATH, "");
        String libs = manager.getPreference(SADIPreferencePanel.PERL_5LIB_DIR, "");
        String scriptDir = manager.getPreference(SADIPreferencePanel.PERL_SADI_SCRIPTS_DIR, "");

        // TODO make sure that the service name is not blank!
        Generator gen = new Generator(perl, libs, scriptDir);
        String str = "";
        try {
            str = gen.generateDatatypes(filename);
        } catch (IOException ioe) {
            manager.saveBooleanPreference(SadiServiceGeneratorView.DO_DATATYPE_GENERATION, false);
        } catch (InterruptedException ie) {
            manager.saveBooleanPreference(SadiServiceGeneratorView.DO_DATATYPE_GENERATION, false);
        }
        return str;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jdesktop.swingworker.SwingWorker#done()
     */
    protected void done() {
        super.done();
        manager.saveBooleanPreference(SadiServiceGeneratorView.DO_DATATYPE_GENERATION, false);
    }

}

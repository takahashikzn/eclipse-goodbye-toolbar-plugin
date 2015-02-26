package jp.root42.eclipse.plugins.goodbye_toolbar;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


@SuppressWarnings("javadoc")
public class Activator
    extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "jp.root42.eclipse.plugins.goodbye_toolbar"; //$NON-NLS-1$

    private static Activator plugin;

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }

    public static ImageDescriptor getImageDescriptor(final String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}

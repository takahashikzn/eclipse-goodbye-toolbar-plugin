package jp.root42.eclipse.plugins.goodbye_toolbar;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;


@SuppressWarnings("javadoc")
public class StartupManager
    implements IStartup {

    private static final Debug debug = new Debug();

    @Override
    public void earlyStartup() {

        final IWorkbench workbench = PlatformUI.getWorkbench();

        workbench.addWindowListener(new IWindowListener() {

            @Override
            public void windowOpened(final IWorkbenchWindow window) {
                asyncHideToolbar(window);
            }

            @Override
            public void windowDeactivated(final IWorkbenchWindow window) {
                asyncHideToolbar(window);
            }

            @Override
            public void windowClosed(final IWorkbenchWindow window) {
                workbench.removeWindowListener(this);
            }

            @Override
            public void windowActivated(final IWorkbenchWindow window) {

                final IWorkbenchPage page = window.getActivePage();

                page.addPartListener(new IPartListener() {

                    @Override
                    public void partOpened(final IWorkbenchPart part) {
                        asyncHideToolbar(window);
                    }

                    @Override
                    public void partClosed(final IWorkbenchPart part) {
                        page.removePartListener(this);
                    }

                    @Override
                    public void partDeactivated(final IWorkbenchPart part) {
                        asyncHideToolbar(window);
                    }

                    @Override
                    public void partBroughtToTop(final IWorkbenchPart part) {
                        asyncHideToolbar(window);
                    }

                    @Override
                    public void partActivated(final IWorkbenchPart part) {
                        asyncHideToolbar(window);
                    }
                });
            }
        });
    }

    private static void asyncHideToolbar(final IWorkbenchWindow window) {

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                hideToolbar(window);
            }
        });
    }

    private static void hideToolbar(final IWorkbenchWindow window) {

        final Shell mainShell = window.getShell();

        if (mainShell.getChildren().length == 0) {
            debug.warn("mainShell has no child: %s", mainShell.getText());
        }

        boolean modified = false;

        for (final Control child : mainShell.getChildren()) {

            if (isToolbar(child)) {

                if (child.isVisible()) {
                    child.setVisible(false);
                    modified = true;
                    debug.info("invisible control: %s", child);
                }

                if (child.isEnabled()) {
                    child.setEnabled(false);
                    modified = true;
                    debug.info("disable control: %s", child);
                }
            }
        }

        if (modified) {
            mainShell.layout();
        }
    }

    private static boolean isToolbar(final Control child) {
        return "org-eclipse-ui-main-toolbar".equals(child.getData("org.eclipse.e4.ui.css.id"));
    }

    private static class Debug {

        private static boolean disabled = true;

        private MessageConsoleStream out;

        private MessageConsoleStream console() {
            if (this.out == null) {
                this.out = this.findConsole("GoodByeToolBar").newMessageStream();
            }
            return this.out;
        }

        private MessageConsole findConsole(final String name) {

            final ConsolePlugin plugin = ConsolePlugin.getDefault();
            final IConsoleManager conMan = plugin.getConsoleManager();
            final IConsole[] existing = conMan.getConsoles();
            for (final IConsole element : existing) {
                if (name.equals(element.getName())) {
                    return (MessageConsole) element;
                }
            }

            final MessageConsole newConsole = new MessageConsole(name, null);
            conMan.addConsoles(new IConsole[] { newConsole });

            this.display(newConsole);

            return newConsole;
        }

        private void display(final MessageConsole newConsole) {

            final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            final String id = IConsoleConstants.ID_CONSOLE_VIEW;

            final IConsoleView view;
            try {
                view = (IConsoleView) page.showView(id);
            } catch (final PartInitException e) {
                throw new RuntimeException(e);
            }

            view.display(newConsole);
        }

        public void info(final String msg, final Object... params) {
            if (disabled) {
                return;
            }

            this.console().println(String.format("INFO: " + msg, params));
        }

        public void warn(final Object msg, final Object... params) {
            if (disabled) {
                return;
            }

            this.console().println(String.format("WARN: " + msg, params));
        }
    }
}

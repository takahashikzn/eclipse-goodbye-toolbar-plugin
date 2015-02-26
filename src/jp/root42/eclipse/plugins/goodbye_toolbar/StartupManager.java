package jp.root42.eclipse.plugins.goodbye_toolbar;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
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

    @Override
    public void earlyStartup() {

        Display.getDefault().asyncExec(new ToolbarHideTask());
    }

    private static class ToolbarHideTask
        implements Runnable {

        private final Debug debug = new Debug();

        @Override
        public void run() {

            final Timer t = new Timer(true);

            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    ToolbarHideTask.this.hideToolbar();
                }
            }, 0, 500);
        }

        private void hideToolbar() {
            // for (final IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
            // this.hideToolbar(window);
            // }
            this.hideToolbar(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
        }

        private void hideToolbar(final IWorkbenchWindow window) {
            if (window == null) {
                return;
            }

            this.debug.info("hideToolbar: %s", window);

            final Shell mainShell = window.getShell();
            if (mainShell == null) {
                return;
            }

            if (mainShell.getChildren().length == 0) {
                this.debug.warn("mainShell has no child: %s", mainShell.getText());
            }

            for (final Control child : mainShell.getChildren()) {
                if ((CBanner.class == child.getClass())) {

                    if (child.isVisible()) {
                        child.setVisible(false);
                        this.debug.info("invisible control: %s", child);
                    }

                    if (child.isEnabled()) {
                        child.setEnabled(false);
                        this.debug.info("disable control: %s", child);
                    }

                }
            }

            mainShell.layout();
        }
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
